from xdcs.servicers.Agent import Agent
from xdcs_api import agent_api_pb2_grpc


class Servicers(object):
    @staticmethod
    def register_all(server):
        agent_api_pb2_grpc.add_AgentServicer_to_server(Agent(), server)
