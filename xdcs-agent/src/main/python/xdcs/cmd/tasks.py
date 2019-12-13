import logging
import os
import stat
import tempfile
from os import path

from xdcs.app import xdcs
from xdcs.cmd import Command
from xdcs.cmd.object_repository import FetchDeploymentCmd, DumpObjectRepositoryTreeCmd
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

        should_allocate_pseudo_tty = deployment['config']['allocate-tty']

        docker_cli = DockerCli()
        if should_allocate_pseudo_tty:
            docker_cli.allocate_pseudo_tty()
        docker_cli \
            .remove_container_after_finish() \
            .nvidia_all_devices() \
            .container_name('xdcs_' + self._task_id) \
            .run(image_id, self._log_handler)

        xdcs().execute(ReportTaskCompletionCmd(self._task_id, self._log_handler))


class RunScriptTaskCmd(_RunDeploymentBasedTaskCmd):
    def execute(self):
        deployment = self._deployment
        script_path = path.join(self._workspace_path, deployment['config'].get('scriptfile'))

        if script_path is None:
            raise TaskExecutionException('Script path is empty')

        # TODO: this is currently needed only because we cannot set
        #   file permissions in GUI yet
        st = os.stat(script_path)
        os.chmod(script_path, st.st_mode | stat.S_IEXEC)

        exec_cmd([script_path], self._log_handler)

        xdcs().execute(ReportTaskCompletionCmd(self._task_id, self._log_handler))


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
