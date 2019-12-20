from xdcs.app import xdcs
from xdcs.cmd import Command
from xdcs.resources import manager
from xdcs_api.common_pb2 import CPU, OPENCL, CUDA
from xdcs_api.registration_pb2 import AgentRegistrationRequest
from xdcs_api.registration_pb2_grpc import AgentRegistrationStub


class RegistrationFailedException(Exception):
    pass


class RegisterAgentCmd(Command):
    def execute(self):
        request = AgentRegistrationRequest()
        request.displayName = xdcs().config('agent.name')
        request.resources.extend(self._get_resources())

        stub = AgentRegistrationStub(xdcs().channel())
        response = stub.Register(request)

        if not response.success:
            raise RegistrationFailedException()

    def _get_resources(self) -> [AgentRegistrationRequest.Resource]:
        ret = []
        for resource in manager.all_resources():
            res = AgentRegistrationRequest.Resource()
            res.key = resource.key()
            res.type = self._map_res_type(resource)
            ret += [res]
        return ret

    def _map_res_type(self, resource):
        if resource.is_cpu():
            return CPU
        elif resource.is_nvidia():
            return CUDA
        else:
            return OPENCL
