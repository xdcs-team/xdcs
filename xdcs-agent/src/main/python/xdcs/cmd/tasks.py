import logging
import tempfile
from os import path

from xdcs.app import xdcs
from xdcs.cmd import Command
from xdcs.cmd.object_repository import FetchDeploymentCmd, DumpObjectRepositoryTreeCmd
from xdcs.cmd.task_reporting import ReportTaskCompletionCmd
from xdcs.docker import DockerCli
from xdcs.log_handling import UploadingLogHandler, PassThroughLogHandler

logger = logging.getLogger(__name__)


class RunTaskCmd(Command):
    _deployment_id: str
    _task_id: str

    def __init__(self, deployment_id: str, task_id: str) -> None:
        self._deployment_id = deployment_id
        self._task_id = task_id

    def execute(self):
        logger.info('Running a deployment: ' + self._deployment_id)
        with tempfile.TemporaryDirectory() as workspace_path:
            xdcs().execute(FetchDeploymentCmd(self._deployment_id))
            deployment: dict = xdcs().object_repository().cat_json(self._deployment_id)
            root_id = deployment['root']
            xdcs().execute(DumpObjectRepositoryTreeCmd(root_id, workspace_path))
            config_type = deployment['config']['type']
            if config_type == 'docker':
                xdcs().execute(RunDockerTaskCmd(workspace_path, deployment, self._deployment_id, self._task_id))
            else:
                raise Exception('Unsupported task type ' + config_type)
        logger.info('Deployment finished: ' + self._deployment_id)


class RunDockerTaskCmd(Command):
    _task_id: str
    _deployment_id: str
    _deployment: dict

    def __init__(self, workspace_path: str, deployment: dict, deployment_id: str, task_id: str) -> None:
        self._workspace_path = workspace_path
        self._deployment_id = deployment_id
        self._deployment = deployment
        self._task_id = task_id

    def execute(self):
        deployment = self._deployment
        dockerfile = deployment['config'].get('dockerfile', None)

        if dockerfile is None or len(dockerfile) == 0:
            dockerfile = 'Dockerfile'

        dockerfile = path.join(self._workspace_path, dockerfile)
        image_id = DockerCli().build(self._workspace_path, dockerfile)
        logger.info('Docker built, image ID: ' + image_id)

        with UploadingLogHandler(self._task_id) as log_handler:
            DockerCli() \
                .remove_container_after_finish() \
                .nvidia_all_devices() \
                .container_name('xdcs_' + self._deployment_id) \
                .run(image_id, log_handler.combine(PassThroughLogHandler(logger)))

        xdcs().execute(ReportTaskCompletionCmd(self._task_id))
