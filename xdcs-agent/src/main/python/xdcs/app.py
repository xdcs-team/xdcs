from concurrent import futures

import grpc
import time

from xdcs.servicers.Servicers import Servicers
from xdcs.utils.rforward import rforward
from xdcs_api.agent_api_pb2 import AgentRegistrationRequest
from xdcs_api.agent_api_pb2_grpc import ServerStub


def run(out):
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=10))
    Servicers.register_all(server)
    server.add_insecure_port('0.0.0.0:' + str(12122))
    server.start()
    with rforward(12122, '127.0.0.1', 32082):
        channel = grpc.insecure_channel('127.0.0.1:32081')
        server_stub = ServerStub(channel)
        print("Requesting register")
        server_stub.RegisterAgent(AgentRegistrationRequest(displayName=('asdf')))
        print("Done")
