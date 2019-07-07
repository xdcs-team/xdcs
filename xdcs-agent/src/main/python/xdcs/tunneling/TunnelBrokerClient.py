import logging
import random
from concurrent import futures
from logging import Logger

import grpc
from grpc._channel import _Rendezvous
from grpc._server import _Server

from xdcs.servicers.Servicers import Servicers
from xdcs.tunneling.ProxyClient import ProxyClient
from xdcs_api import agent_api_pb2_grpc
from xdcs_api.agent_api_pb2 import TunneledMessage


class TunnelBrokerClient:
    logger: Logger
    port: int
    server: _Server

    def __init__(self) -> None:
        self.logger = logging.getLogger(TunnelBrokerClient.__name__)
        self.port = random.randint(1024, 65536)

        self.server = grpc.server(futures.ThreadPoolExecutor(max_workers=1))
        Servicers.register_all(self.server)
        self.server.add_insecure_port('127.0.0.1:' + str(self.port))

    def start_tunneling(self, channel):
        self.server.start()
        tunneling_stub = agent_api_pb2_grpc.TunnelBrokerStub(channel)
        proxy_client = ProxyClient(self.port)
        proxy_client.set_error_handler(self.__handle_error)

        def read_messages():
            for chunk in proxy_client.read_chunks():
                self.logger.debug("Proxying client->server: " + str(chunk))
                yield TunneledMessage(data=chunk)

        def sent_messages():
            for message in tunneling_stub.Tunnel(read_messages()):
                chunk = message.data
                self.logger.debug("Proxying server->client: " + str(chunk))
                yield chunk

        proxy_client.send_chunks(sent_messages())

    def __handle_error(self, error: Exception) -> None:
        if isinstance(error, _Rendezvous):
            if error.code() == grpc.StatusCode.CANCELLED:
                return

        raise error
