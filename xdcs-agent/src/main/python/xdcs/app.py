from concurrent import futures

import grpc
import time

from xdcs.servicers.Servicers import Servicers
from xdcs.utils.rforward import rforward


def run(out):
    server = grpc.server(futures.ThreadPoolExecutor(max_workers=2))
    Servicers.register_all(server)
    server.add_insecure_port('0.0.0.0:' + str(12122))
    server.start()
    with rforward(12122, '127.0.0.1', 32082):
        try:
            while True: time.sleep(10)
        except KeyboardInterrupt:
            return
