from xdcs_api.agent_api_pb2 import TaskExecutionResult
from xdcs_api.agent_api_pb2_grpc import AgentServicer


class Agent(AgentServicer):
    def ExecuteTask(self, request, context):
        print("WOW")
        return TaskExecutionResult(success=1)
