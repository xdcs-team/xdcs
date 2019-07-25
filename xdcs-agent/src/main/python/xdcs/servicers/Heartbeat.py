from xdcs_api.agent_heartbeat_pb2 import HeartbeatResponse
from xdcs_api.agent_heartbeat_pb2_grpc import HeartbeatServicer


class Heartbeat(HeartbeatServicer):
    def Heartbeat(self, request_iterator, context):
        return HeartbeatResponse()
