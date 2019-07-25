from xdcs.servicers.Agent import Agent
from xdcs.servicers.AgentInfo import AgentInfo
from xdcs.servicers.Heartbeat import Heartbeat
from xdcs_api.agent_api_pb2_grpc import add_AgentServicer_to_server
from xdcs_api.agent_heartbeat_pb2_grpc import add_HeartbeatServicer_to_server
from xdcs_api.agent_info_pb2_grpc import add_AgentInfoServicer_to_server


class Servicers(object):
    @staticmethod
    def register_all(server):
        add_HeartbeatServicer_to_server(Heartbeat(), server)
        add_AgentServicer_to_server(Agent(), server)
        add_AgentInfoServicer_to_server(AgentInfo(), server)
