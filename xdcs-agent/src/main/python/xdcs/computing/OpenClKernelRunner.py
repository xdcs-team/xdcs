import pyopencl as cl

from xdcs.computing.KernelRunner import KernelRunner


class OpenClKernelRunner(KernelRunner):

    @staticmethod
    def run(kernel_program, kernel_name, ctx, global_work_shape, local_work_shape, kernel_arguments) -> cl.CommandQueue:
        prg = cl.Program(ctx, kernel_program).build()
        queue: cl.CommandQueue = cl.CommandQueue(ctx)
        kernel = getattr(prg, kernel_name)
        launch = kernel(queue, global_work_shape, local_work_shape, *kernel_arguments)
        launch.wait()
        return queue
