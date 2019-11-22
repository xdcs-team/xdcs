from xdcs.app import xdcs
from xdcs.cmd.tasks import RunTaskCmd
from xdcs.decorators import asynchronous
from xdcs_api.agent_execution_pb2_grpc import TaskRunnerServicer
from xdcs_api.common_pb2 import OkResponse


class TaskRunner(TaskRunnerServicer):
    def Submit(self, request, context):
        deployment_id = request.deploymentId
        self.__execute_async(deployment_id)
        return OkResponse()

    @asynchronous
    def __execute_async(self, deployment_id):
        xdcs().execute(RunTaskCmd(deployment_id))
