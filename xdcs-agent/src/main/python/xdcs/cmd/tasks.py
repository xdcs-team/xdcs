import logging
import os
import shutil
import stat
import tempfile
from os import path
from typing import Optional

from xdcs.app import xdcs
from xdcs.cmd import Command
from xdcs.cmd.object_repository import FetchDeploymentCmd, DumpObjectRepositoryTreeCmd, \
    MaterializeTreeToObjectRepositoryCmd, UploadObjectsCmd
from xdcs.cmd.task_reporting import ReportTaskCompletionCmd, ReportTaskFailureCmd
from xdcs.docker import DockerCli
from xdcs.exec import exec_cmd
from xdcs.log_handling import UploadingLogHandler, PassThroughLogHandler, LogHandler, LogLevel

logger = logging.getLogger(__name__)


class TaskExecutionException(Exception):
    pass


class RunTaskCmd(Command):
    _deployment_id: str
    _task_id: str

    def __init__(self, deployment_id: str, task_id: str) -> None:
        self._deployment_id = deployment_id
        self._task_id = task_id

    def execute(self):
        with UploadingLogHandler(self._task_id) as log_handler:
            log_handler = log_handler.combine(PassThroughLogHandler(logger))
            log_handler.internal_log("Running a deployment: " + self._deployment_id)

            self._execute(log_handler)

    def _execute(self, log_handler):
        with tempfile.TemporaryDirectory() as workspace_path:
            xdcs().execute(FetchDeploymentCmd(self._deployment_id))
            deployment: dict = xdcs().object_repository().cat_json(self._deployment_id)
            root_id = deployment['root']
            xdcs().execute(DumpObjectRepositoryTreeCmd(root_id, workspace_path))
            config_type = deployment['config']['type']

            constructor_args = [workspace_path, deployment, self._deployment_id, self._task_id, log_handler]
            if config_type == 'docker':
                xdcs().execute(HandleExceptionCmd(RunDockerTaskCmd(*constructor_args), self._task_id, log_handler))
            elif config_type == 'script':
                xdcs().execute(HandleExceptionCmd(RunScriptTaskCmd(*constructor_args), self._task_id, log_handler))
            else:
                raise Exception('Unsupported task type ' + config_type)
            log_handler.internal_log("Deployment finished: " + self._deployment_id)


class _RunDeploymentBasedTaskCmd(Command):
    _task_id: str
    _deployment_id: str
    _deployment: dict
    _workspace_path: str
    _log_handler: LogHandler

    def __init__(self, workspace_path: str, deployment: dict, deployment_id: str, task_id: str,
                 log_handler: LogHandler) -> None:
        self._workspace_path = workspace_path
        self._deployment_id = deployment_id
        self._deployment = deployment
        self._task_id = task_id
        self._log_handler = log_handler


class RunDockerTaskCmd(_RunDeploymentBasedTaskCmd):
    def execute(self):
        deployment = self._deployment
        dockerfile = deployment['config'].get('dockerfile', None)

        if dockerfile is None or len(dockerfile) == 0:
            dockerfile = 'Dockerfile'

        dockerfile = path.join(self._workspace_path, dockerfile)
        image_id = DockerCli().build(self._workspace_path, dockerfile)
        self._log_handler.internal_log('Docker built, image ID: ' + image_id)

        artifact_root = self._run_image(image_id)
        xdcs().execute(ReportTaskCompletionCmd(self._task_id, self._log_handler, artifact_root))

    def _run_image(self, image_id: str) -> Optional[str]:
        should_allocate_pseudo_tty = self._deployment['config'].get('allocate-tty', True)

        with tempfile.TemporaryDirectory() as artifacts_root:
            cid = None
            try:
                docker_cli = DockerCli()
                if should_allocate_pseudo_tty:
                    docker_cli.allocate_pseudo_tty()
                cid = docker_cli \
                    .nvidia_all_devices() \
                    .container_name('xdcs_' + self._task_id) \
                    .allocate_pseudo_tty() \
                    .run(image_id, self._log_handler)

                artifact_root = self._handle_artifacts(cid, artifacts_root)
                return artifact_root
            finally:
                if cid:
                    DockerCli().rm(cid)

    def _handle_artifacts(self, cid: str, artifacts_root) -> Optional[str]:
        artifacts = self._deployment['config'].get('artifacts', [])
        if len(artifacts) == 0:
            return None

        for artifact in artifacts:
            dest = os.path.join(artifacts_root, artifact)
            DockerCli().cp(cid, artifact, dest)

        root_id, all_objects = MaterializeTreeToObjectRepositoryCmd(artifacts_root).execute()
        UploadObjectsCmd(all_objects).execute()
        return root_id


class RunScriptTaskCmd(_RunDeploymentBasedTaskCmd):
    def execute(self):
        deployment = self._deployment
        script_path = path.join(self._workspace_path, self._get_script_path(deployment))
        script_path = path.normpath(script_path)
        if not script_path.startswith(self._workspace_path):
            raise TaskExecutionException('Path traversal detected')

        if script_path is None:
            raise TaskExecutionException('Script path is empty')

        # TODO: this is currently needed only because we cannot set
        #   file permissions in GUI yet
        st = os.stat(script_path)
        os.chmod(script_path, st.st_mode | stat.S_IEXEC)

        exec_cmd([script_path], self._log_handler)

        artifact_root = self._gather_artifacts()
        xdcs().execute(ReportTaskCompletionCmd(self._task_id, self._log_handler, artifact_root))

    def _get_script_path(self, deployment):
        scriptfile = deployment['config'].get('scriptfile')
        if scriptfile.startswith('/'):
            return scriptfile[1:]
        return scriptfile

    def _gather_artifacts(self) -> Optional[str]:
        with tempfile.TemporaryDirectory() as artifacts_root:
            artifacts = self._deployment['config'].get('artifacts', [])
            if len(artifacts) == 0:
                return None

            for artifact in artifacts:
                dest = os.path.join(artifacts_root, artifact)
                shutil.copy(artifact, dest)

            root_id, all_objects = MaterializeTreeToObjectRepositoryCmd(artifacts_root).execute()
            UploadObjectsCmd(all_objects).execute()
            return root_id


class HandleExceptionCmd(Command):
    _delegate: Command
    _task_id: str
    _log_handler: LogHandler

    def __init__(self, delegate: Command, task_id: str, log_handler: LogHandler) -> None:
        self._delegate = delegate
        self._task_id = task_id
        self._log_handler = log_handler

    def execute(self):
        try:
            self._delegate.execute()
        except Exception as e:
            self._log_handler.internal_log("Error while executing task: " + str(e), LogLevel.ERROR)
            xdcs().execute(ReportTaskFailureCmd(self._task_id, self._log_handler))
            raise e
