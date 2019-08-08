import grpc

from xdcs import session
from xdcs.utils import interceptors
from xdcs_api.agent_api_pb2 import AgentRegistrationRequest
from xdcs_api.agent_api_pb2_grpc import ServerStub
from xdcs_api.agent_security_pb2 import SecurityInformationResponse, SecurityInformation
from xdcs_api.agent_security_pb2_grpc import AgentSecurityServicer


class AgentSecurity(AgentSecurityServicer):
    def AcceptSecurityInformation(self, request: SecurityInformation, context):
        token = request.tokenGrant.token
        session.set_token(token)
        token_interceptor = interceptors.header_adder_interceptor('authorization', token)

        channel = grpc.insecure_channel('127.0.0.1:32081')
        intercepted_channel = grpc.intercept_channel(channel, token_interceptor)
        session.set_channel(intercepted_channel)

        server_stub = ServerStub(intercepted_channel)
        print("Requesting register")
        server_stub.RegisterAgent(AgentRegistrationRequest(displayName=('asdf')))
        print("Done")

        return SecurityInformationResponse()
