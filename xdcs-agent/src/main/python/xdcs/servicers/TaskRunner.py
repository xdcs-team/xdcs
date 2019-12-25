import logging

from xdcs.app import xdcs
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
        self.__execute_async(deployment_id, task_id, agent_variables)
        return OkResponse()

    @asynchronous
    def __execute_async(self, deployment_id, task_id, agent_variables):
        try:
            with UploadingLogHandler(task_id) as log_handler:
                log_handler = log_handler.combine(PassThroughLogHandler(logger))
                log_handler.internal_log('Starting task execution: {}'.format(task_id))
                log_handler.internal_log('Running with deployment: {}'.format(deployment_id))
                try:
                    xdcs().execute(RunTaskCmd(deployment_id, task_id, agent_variables, log_handler))
                except Exception as e:
                    if log_handler:
                        log_handler.internal_log("Error while executing task: " + str(e), LogLevel.ERROR)
                        log_handler.internal_log("Reporting task failure: " + str(task_id), LogLevel.ERROR)
                    raise e
        except Exception as e:
            xdcs().execute(ReportTaskFailureCmd(task_id))
            raise e
