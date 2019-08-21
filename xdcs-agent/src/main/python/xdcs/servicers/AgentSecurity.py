import grpc

from xdcs.app import xdcs
from xdcs.cmd.registration import RegisterAgentCmd
from xdcs.decorators import asynchronous
from xdcs.utils import interceptors
from xdcs_api.agent_security_pb2 import SecurityInformationResponse, SecurityInformation
from xdcs_api.agent_security_pb2_grpc import AgentSecurityServicer


class AgentSecurity(AgentSecurityServicer):
    def AcceptSecurityInformation(self, request: SecurityInformation, context):
        token = request.tokenGrant.token
        xdcs().set_token(token)
        token_interceptor = interceptors.header_adder_interceptor('authorization', token)

        server_host = xdcs().config('server.host')
        server_port = xdcs().config('server.port.grpc')
        channel = grpc.insecure_channel(server_host + ':' + str(server_port))
        intercepted_channel = grpc.intercept_channel(channel, token_interceptor)
        xdcs().set_channel(intercepted_channel)

        self._register_async()

        return SecurityInformationResponse()

    @asynchronous
    def _register_async(self):
        xdcs().execute(RegisterAgentCmd())
