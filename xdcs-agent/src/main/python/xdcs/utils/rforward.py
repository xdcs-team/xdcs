import logging
import socket
import threading
from typing import Optional

import paramiko
from paramiko import Transport

from xdcs.app import xdcs
from xdcs.utils.sockutils import couple_socks

logger = logging.getLogger(__name__)


class _ReverseForwardContext:
    transport: Optional[Transport]

    def __init__(self,
                 local_port: int,
                 server_addr: str,
                 server_port: int,
                 asynchronous: bool) -> None:
        self.asynchronous = asynchronous
        self.local_port = local_port
        self.server_addr = server_addr
        self.server_port = server_port
        self.thread = None
        self.transport = None

    def __enter__(self):
        logger.debug("Connecting to " + self.server_addr + ":" + str(self.server_port))

        server_auth_name = xdcs().config('server.auth.name')
        server_auth_key = xdcs().config('server.auth.key', None)
        server_auth_password = xdcs().config('server.auth.password', None)

        client = paramiko.SSHClient()

        if xdcs().config('server.allow_unknown_hosts', False):
            client.set_missing_host_key_policy(paramiko.WarningPolicy())
        else:
            client.set_missing_host_key_policy(paramiko.RejectPolicy())

        private_key = paramiko.RSAKey.from_private_key_file(server_auth_key) if server_auth_key else None
        client.connect(
                self.server_addr, self.server_port,
                username=server_auth_name,
                pkey=private_key,
                password=server_auth_password,
                look_for_keys=False,
        )
        logger.debug("Connected to server's sshd")

        self.transport = client.get_transport()

        remote_port = self._request_port_forward()
        logger.debug("Now forwarding remote port %d to local port %d", remote_port, self.local_port)

        if self.asynchronous:
            self.thread = threading.Thread(target=self._transport_thread, args=())
            self.thread.setDaemon(True)
            self.thread.start()
        else:
            self._transport_thread()

    def _request_port_forward(self):
        # Note: due to a bug in paramiko, we need to explicitly
        # set the _tcp_handler BEFORE requesting port forwarding.
        # A race condition may occur, because of the faulty request
        # forwarding implementation in paramiko:
        #   1. request tcpip-forward
        #   2. wait for server response
        #   3. read the allocated port
        #   4. set the channel handler
        # If the server manages to start a channel before 4, it gets
        # rejected because no handler is present.
        # That's why we have to manually set the handler even before
        # invoking the request_port_forward method.
        self.transport._tcp_handler = lambda channel, src_addr, dest_addr_port: \
            self.transport._queue_incoming_channel(channel)

        remote_port = self.transport.request_port_forward("127.0.0.1", 0)
        return remote_port

    def _transport_thread(self):
        channel = None
        while channel is None:
            channel = self.transport.accept(1000)

        self._handle_opened_channel(channel)

    def _handle_opened_channel(self, channel):
        sock = socket.socket()
        try:
            sock.connect(('127.0.0.1', self.local_port))
        except Exception as e:
            logger.error("Forwarding request to local port %d failed: %r", self.local_port, e)
            return

        logger.debug("Tunnel opened %r -> %r -> %r", channel.origin_addr, channel.getpeername(),
                     ('127.0.0.1', self.local_port))
        couple_socks(sock, channel)
        logger.debug("Tunnel closed from %r", channel.origin_addr)

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.transport.close()


def rforward(
        local_port: int,
        server_addr: str,
        server_port: int,
        asynchronous: bool = True) -> _ReverseForwardContext:
    return _ReverseForwardContext(local_port, server_addr, server_port, asynchronous)
