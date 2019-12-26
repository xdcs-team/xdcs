import pyopencl as cl

from xdcs.computing.OpenClKernelRunner import OpenClKernelRunner
from xdcs.parameters import KernelParameter, KernelParameters, ParameterDirection
from xdcs_api.kernel_runner_pb2 import KernelRunnerResponse, KernelTask, KernelRunnerResponseParameter
from xdcs_api.kernel_runner_pb2_grpc import KernelRunnerServicer


class KernelRunner(KernelRunnerServicer):
    def RunTask(self, request: KernelTask, context):
        kernel_parameters = list()

        for parameter in request.parameters:
            kernel_parameters.append(
                KernelParameter(parameter.direction, parameter.value, parameter.position, parameter.type,
                                parameter.name))

        ctx = cl.create_some_context()

        OpenClKernelRunner.run(request.program, request.funcName, request.threads,
                               KernelParameters(kernel_parameters), ctx)

        return_parameters = list()

        for parameter in request.parameters:
            if parameter.type != ParameterDirection.IN:
                return_parameters.append(KernelRunnerResponseParameter(parameter.name, parameter.value))

        return KernelRunnerResponse(return_parameters)
