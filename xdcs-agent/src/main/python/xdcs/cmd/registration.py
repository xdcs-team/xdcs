from xdcs.app import xdcs
from xdcs.cmd import Command
from xdcs_api.registration_pb2 import AgentRegistrationRequest
from xdcs_api.registration_pb2_grpc import AgentRegistrationStub


class RegistrationFailedException(Exception):
    pass


class RegisterAgentCmd(Command):
    def execute(self):
        request = AgentRegistrationRequest()
        request.displayName = xdcs().config('agent.name')

        stub = AgentRegistrationStub(xdcs().channel())
        response = stub.Register(request)

        if not response.success:
            raise RegistrationFailedException()
