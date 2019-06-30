import time

import grpc

from xdcs.tunneling.TunnelBrokerClient import TunnelBrokerClient


def test_server():
    channel = grpc.insecure_channel('127.0.0.1:32081')
    TunnelBrokerClient().start_tunneling(channel)
    time.sleep(1)
    print("Closing")
    time.sleep(0.1)
    channel.close()


def run(out):
    test_server()
