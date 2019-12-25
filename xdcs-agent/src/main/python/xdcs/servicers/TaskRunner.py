import logging

from xdcs.app import xdcs
from xdcs.cmd.merge import MergeTaskCmd
from xdcs.cmd.task_reporting import ReportTaskFailureCmd
from xdcs.cmd.tasks import RunTaskCmd
from xdcs.decorators import asynchronous
from xdcs.log_handling import UploadingLogHandler, PassThroughLogHandler, LogLevel
from xdcs_api.agent_execution_pb2_grpc import TaskRunnerServicer
from xdcs_api.common_pb2 import OkResponse

logger = logging.getLogger(__name__)


class TaskRunner(TaskRunnerServicer):
    def Submit(self, request, context):
        deployment_id = request.deploymentId
        task_id = request.taskId
        agent_variables = request.agentVariables
        kernel_config = request.kernelConfig
        self.__execute_async(deployment_id, task_id, agent_variables, kernel_config)
        return OkResponse()

    def Merge(self, request, context):
        deployment_id = request.deploymentId
        task_id = request.taskId
        origin_task_id = request.originTaskId
        artifact_trees = request.artifactTrees
        self.__merge_async(deployment_id, task_id, origin_task_id, artifact_trees)
        return OkResponse()

    @asynchronous
    def __execute_async(self, deployment_id, task_id, agent_variables, kernel_config):
        try:
            with UploadingLogHandler(task_id) as log_handler:
                log_handler = log_handler.combine(PassThroughLogHandler(logger))
                log_handler.internal_log('Starting task execution: {}'.format(task_id))
                log_handler.internal_log('Running with deployment: {}'.format(deployment_id))
                try:
                    xdcs().execute(RunTaskCmd(deployment_id, task_id, agent_variables, kernel_config, log_handler))
                except Exception as e:
                    if log_handler:
                        log_handler.internal_log("Error while executing task: " + str(e), LogLevel.ERROR)
                        log_handler.internal_log("Reporting task failure: " + str(task_id), LogLevel.ERROR)
                    raise e
        except Exception as e:
            xdcs().execute(ReportTaskFailureCmd(task_id))
            raise e

    @asynchronous
    def __merge_async(self, deployment_id, task_id, origin_task_id, artifact_trees):
        try:
            with UploadingLogHandler(task_id) as log_handler:
                log_handler = log_handler.combine(PassThroughLogHandler(logger))
                log_handler.internal_log('Starting merge task execution with id: {}'.format(task_id))
                log_handler.internal_log('Task that is being merged: {}'.format(origin_task_id))
                log_handler.internal_log('Running with deployment: {}'.format(deployment_id))
                try:
                    xdcs().execute(MergeTaskCmd(deployment_id, task_id, artifact_trees, log_handler))
                except Exception as e:
                    if log_handler:
                        log_handler.internal_log("Error while executing merge task: " + str(e), LogLevel.ERROR)
                        log_handler.internal_log("Reporting merge task failure: " + str(task_id), LogLevel.ERROR)
                    raise e
        except Exception as e:
            xdcs().execute(ReportTaskFailureCmd(task_id))
            raise e
