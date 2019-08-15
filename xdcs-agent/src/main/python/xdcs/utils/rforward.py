import logging
import socket
import threading

import paramiko

from xdcs.utils.sockutils import couple_socks

logger = logging.getLogger(__name__)


class _ReverseForwardContext:
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

    def __enter__(self):
        logger.debug("Connecting to " + self.server_addr + ":" + str(self.server_port))

        client = paramiko.SSHClient()
        client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        client.connect(
            self.server_addr, self.server_port,
            username='user',
            pkey=paramiko.RSAKey.from_private_key_file('./dev/key'),
            look_for_keys=False,
        )
        logger.debug("Connected to server's sshd")

        self.transport = client.get_transport()
        remote_port = self.transport.request_port_forward("127.0.0.1", 0)
        logger.debug("Now forwarding remote port %d to local port %d", remote_port, self.local_port)

        if self.asynchronous:
            self.thread = threading.Thread(target=self._transport_thread, args=())
            self.thread.setDaemon(True)
            self.thread.start()
        else:
            self._transport_thread()

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
