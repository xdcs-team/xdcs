from concurrent.futures import Executor, ThreadPoolExecutor

import grpc
from grpc._interceptor import _Channel

from xdcs.cmd import Command
from xdcs.servicers.Servicers import Servicers
from xdcs.utils.rforward import rforward


class _XDCS:
    _token: str
    _channel: _Channel
    _executor: Executor

    def __init__(self) -> None:
        self._executor = ThreadPoolExecutor(max_workers=10)

    def executor(self) -> Executor:
        return self._executor

    def execute(self, command: Command) -> None:
        self.executor().submit(command.execute)

    def run(self) -> None:
        server = grpc.server(self.executor())
        Servicers.register_all(server)
        server.add_insecure_port('0.0.0.0:' + str(12122))
        server.start()
        with rforward(12122, '127.0.0.1', 32082, False):
            pass

    def set_token(self, token: str) -> None:
        self._token = token

    def token(self) -> str:
        return self._token

    def set_channel(self, channel) -> None:
        self._channel = channel

    def channel(self):
        return self._channel
