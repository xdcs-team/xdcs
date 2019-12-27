import os
from enum import Enum

import numpy as np
import pyopencl as cl
import xdcs.computing.OpenClKernelRunner as Compiler

from xdcs.app import xdcs
from xdcs.cmd.object_repository import RetrieveObjectsCmd
from xdcs_api.agent_execution_pb2 import KernelConfig


class KernelArgument:
    class Direction(Enum):
        IN = 'IN'
        OUT = 'OUT'
        IN_OUT = 'IN_OUT'

    class Type(Enum):
        SIMPLE = 'SIMPLE'
        POINTER = 'POINTER'

    _name: str
    _obj_id: str
    _position: int
    _direction: Direction
    _type: Type

    def __init__(self, name: str, obj_id: str, position: int, direction: Direction, arg_type: Type):
        self._name = name
        self._obj_id = obj_id
        self._position = position
        self._direction = direction
        self._type = arg_type

    @staticmethod
    def create(obj_id: str, kernel_param: dict, position: int):
        return KernelArgument(
            kernel_param['name'],
            obj_id,
            position,
            KernelArgument.Direction(kernel_param['direction']),
            KernelArgument.Type(kernel_param['type'])
        )

    @property
    def name(self):
        return self._name

    @property
    def obj_id(self):
        return self._obj_id

    def get_or_value(self) -> bytes:
        return xdcs().object_repository().cat_bytes(self._obj_id)

    @property
    def position(self):
        return self._position

    @property
    def direction(self):
        return self._direction

    @property
    def type(self):
        return self._type


class OpenClKernelConfig:
    reserved_arg_names: [str] = ['XDCS_AGENT_ID', 'XDCS_AGENT_COUNT']
    _arguments: [KernelArgument]
    _global_work_shape: tuple
    _local_work_shape: tuple

    def __init__(self, arguments: [KernelArgument], global_work_shape: tuple, local_work_shape: tuple):
        self._arguments = arguments
        self._global_work_shape = global_work_shape
        self._local_work_shape = local_work_shape

    @property
    def arguments(self):
        return self._arguments

    @property
    def global_work_shape(self):
        return self._global_work_shape

    @property
    def local_work_shape(self):
        return self._local_work_shape

    @staticmethod
    def parse_config(grpc_config: KernelConfig, kernel_params: [dict]):
        arguments: [KernelArgument] = []
        for grpc_argument, kernel_param, position in zip(grpc_config.kernelArguments, kernel_params,
                                                         range(0, len(kernel_params))):
            arguments.append(KernelArgument.create(grpc_argument, kernel_param, position))
        global_work_shape = tuple(grpc_config.globalWorkShape)
        local_work_shape = None
        if grpc_config.localWorkShape is not None and len(grpc_config.localWorkShape) == 3:
            local_work_shape = tuple(grpc_config.localWorkShape)
        return OpenClKernelConfig(arguments, global_work_shape, local_work_shape)


class KernelManager:
    _kernel_config: OpenClKernelConfig
    _host_argument_values: [object]
    _device_argument_values: [object]
    _queue: cl.CommandQueue
    _ctx: cl.Context

    def __init__(self, kernel_config: OpenClKernelConfig):
        self._kernel_config = kernel_config
        self._host_argument_values = []
        self._device_argument_values = []

    @staticmethod
    def from_config(grpc_config: KernelConfig, kernel_params: [dict]):
        return KernelManager(OpenClKernelConfig.parse_config(grpc_config, kernel_params))

    @property
    def kernel_config(self):
        return self._kernel_config

    @property
    def device_argument_values(self):
        return self._device_argument_values

    def prepare_execution(self, agent_variables: dict):
        self.fetch_argument_values()
        self.initialize_context()
        self.prepare_argument_values(agent_variables)

    def execute_kernel(self, kernel_source: str, kernel_name: str):
        self._queue = Compiler.OpenClKernelRunner.run(
            kernel_source,
            kernel_name,
            self._ctx,
            self.kernel_config.global_work_shape,
            self.kernel_config.local_work_shape,
            self.device_argument_values
        )

    def initialize_context(self):
        # TODO workaround - prevent opencl from asking to choose platform
        os.environ['PYOPENCL_CTX'] = '0'
        # TODO context creation should take into account selected resources during task creation
        self._ctx = cl.create_some_context()

    def fetch_argument_values(self):
        object_ids: [str] = []
        for argument in self._kernel_config.arguments:
            object_ids.append(argument.obj_id)
        # remove duplicates
        object_ids = list(dict.fromkeys(object_ids))
        xdcs().execute(RetrieveObjectsCmd((ids for ids in [object_ids])))

    def prepare_argument_values(self, agent_variables: dict):
        mf = cl.mem_flags
        for argument in self._kernel_config.arguments:
            if argument.type == KernelArgument.Type.SIMPLE:
                if argument.name in OpenClKernelConfig.reserved_arg_names:
                    if argument.type != KernelArgument.Type.SIMPLE or argument.direction != KernelArgument.Direction.IN:
                        raise Exception('Wrong argument config with reserved name: %s' % str(argument))
                    value = KernelManager.interpret_simple_value(agent_variables[argument.name])
                else:
                    value = KernelManager.interpret_simple_value(argument.get_or_value().decode('utf-8'))
                self._host_argument_values.append(value)
                self._device_argument_values.append(value)
            elif argument.type == KernelArgument.Type.POINTER:
                if argument.direction == KernelArgument.Direction.IN:
                    host_buffer = np.frombuffer(buffer=argument.get_or_value(), dtype=np.int8)
                    device_buffer = cl.Buffer(self._ctx, mf.READ_ONLY | mf.COPY_HOST_PTR, hostbuf=host_buffer)
                elif argument.direction == KernelArgument.Direction.IN_OUT:
                    host_buffer = np.frombuffer(buffer=argument.get_or_value(), dtype=np.int8)
                    device_buffer = cl.Buffer(self._ctx, mf.READ_WRITE | mf.COPY_HOST_PTR, hostbuf=host_buffer)
                elif argument.direction == KernelArgument.Direction.OUT:
                    host_buffer = np.zeros(int(str(argument.get_or_value().decode('utf-8'))), dtype=np.int8)
                    device_buffer = cl.Buffer(self._ctx, mf.WRITE_ONLY, host_buffer.nbytes)
                else:
                    raise Exception('Unrecognized argument: %s, %s' % (argument.direction, argument.type))
                self._host_argument_values.append(host_buffer)
                self._device_argument_values.append(device_buffer)
            else:
                raise Exception('Unrecognized argument: %s, %s' % (argument.direction, argument.type))

    @staticmethod
    def interpret_simple_value(value: str = '0'):
        try:
            return np.int32(value)
        except ValueError:
            return np.float32(value)

    def read_output_arguments(self):
        output_arguments = []
        for argument, host_value, device_value in zip(self._kernel_config.arguments,
                                                      self._host_argument_values, self._device_argument_values):
            if argument.direction != KernelArgument.Direction.IN and argument.type == KernelArgument.Type.POINTER:
                buffer = np.empty_like(host_value)
                cl.enqueue_copy(self._queue, buffer, device_value).wait()
                name = '%d_%s' % (argument.position, argument.name)
                output_arguments.append((name, buffer))
            elif argument.direction != KernelArgument.Direction.IN and argument.type == KernelArgument.Type.SIMPLE:
                raise Exception('Unsupported return argument: %s, %s' % (argument.direction, argument.type))
        self._queue.flush()
        return output_arguments
