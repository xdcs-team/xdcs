import abc


class KernelRunner(abc.ABC):
    @staticmethod
    def run(kernel_program, kernel_name, ctx, global_work_shape, local_work_shape, kernel_arguments):
        pass
