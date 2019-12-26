import abc
from xdcs.parameters import KernelParameters


class KernelRunner(abc.ABC):
    @staticmethod
    def run(program, func_name, threads, program_parameters: KernelParameters, ctx):
        pass
