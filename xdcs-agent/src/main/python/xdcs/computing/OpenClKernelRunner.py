import pyopencl as cl

from xdcs.parameters import KernelParameters
from xdcs.computing.KernelRunner import KernelRunner


class OpenClKernelRunner(KernelRunner):

    @staticmethod
    def run(program, func_name, threads, program_parameters: KernelParameters, ctx):
        prg = cl.Program(ctx, program).build()
        queue = cl.CommandQueue(ctx)
        kernel = getattr(prg, func_name)
        kernel(queue, threads, None, *program_parameters.get_params_values())
        return queue
