from xdcs.servicers.AgentInfo import AgentInfo
from xdcs.servicers.AgentSecurity import AgentSecurity
from xdcs.servicers.Heartbeat import Heartbeat
from xdcs.servicers.TaskRunner import TaskRunner
from xdcs_api.agent_execution_pb2_grpc import add_TaskRunnerServicer_to_server
from xdcs_api.agent_info_pb2_grpc import add_AgentInfoServicer_to_server
from xdcs_api.agent_security_pb2_grpc import add_AgentSecurityServicer_to_server
from xdcs_api.heartbeat_pb2_grpc import add_HeartbeatServicer_to_server


class Servicers(object):
    @staticmethod
    def register_all(server):
        add_HeartbeatServicer_to_server(Heartbeat(), server)
        add_AgentInfoServicer_to_server(AgentInfo(), server)
        add_AgentSecurityServicer_to_server(AgentSecurity(), server)
        add_TaskRunnerServicer_to_server(TaskRunner(), server)
