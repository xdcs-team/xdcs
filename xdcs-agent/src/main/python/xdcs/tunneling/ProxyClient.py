import errno
import socket
from threading import Thread
from typing import Iterator, Callable


class ProxyClient:
    proxy_socket: socket.socket
    error_handler: Callable[[Exception], None]

    def __init__(self, port: int) -> None:
        self.proxy_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.proxy_socket.connect(('127.0.0.1', port))
        self.proxy_socket.setblocking(False)
        self.error_handler = lambda x: None

    def set_error_handler(self, error_handler: Callable[[Exception], None]) -> None:
        self.error_handler = error_handler

    def read_chunk(self) -> bytes:
        return self.proxy_socket.recv(10 * 1024)

    def send_chunk(self, chunk: bytes) -> None:
        self.proxy_socket.send(chunk)

    def read_chunks(self) -> Iterator[bytes]:
        try:
            while True:
                self.proxy_socket.setblocking(True)
                first = self.proxy_socket.recv(1)

                self.proxy_socket.setblocking(False)
                try:
                    rest = self.proxy_socket.recv(10 * 1024)
                except socket.error as e:
                    print(e)
                    err = e.args[0]
                    if err == errno.EAGAIN or err == errno.EWOULDBLOCK:
                        continue
                    else:
                        print(e)
                        break

                message = first + rest

                if len(message) == 0:
                    continue

                yield message
        except Exception as e:
            print("Exception while iterating: " + str(e))
            raise e

    def send_chunks(self, chunks: Iterator[bytes]) -> None:
        thread = Thread(target=self.__send_chunks, args=(chunks,))
        thread.start()

    def __send_chunks(self, chunks: Iterator[bytes]) -> None:
        try:
            for chunk in chunks:
                self.send_chunk(chunk)
        except Exception as e:
            self.error_handler(e)
