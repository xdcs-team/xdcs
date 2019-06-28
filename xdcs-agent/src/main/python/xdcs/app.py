import time

import grpc
from google.protobuf.any_pb2 import Any

from xdcs_api import agent_api_pb2_grpc
from xdcs_api.agent_api_pb2 import AgentRegistrationRequest, Task, OPEN_CL, TaskExecutionResult


def generate():
    try:
        while True:
            any = Any()
            any.Pack(TaskExecutionResult(success=True))
            yield any
            time.sleep(0.1)
    except Exception as e:
        print('Error: ' + str(e))


def test_server():
    channel = grpc.insecure_channel('127.0.0.1:32081')
    stub = agent_api_pb2_grpc.ServerStub(channel)
    result = stub.RegisterAgent(AgentRegistrationRequest(displayName='hello'))
    print(result)

    result = stub.TaskExecution(generate())
    for i in result:
        print(i)


def run(out):
    test_server()
