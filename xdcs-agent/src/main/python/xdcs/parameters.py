from enum import Enum


class KernelParameter:
    def __init__(self, direction, value, position, param_type, name):
        self.direction = direction
        self.value = value
        self.position = position
        self.type = param_type
        self.name = name

    def get_type(self):
        return self.type

    def get_value(self):
        return self.value


class ParameterDirection(Enum):
    IN = 1
    OUT = 2
    IN_OUT = 3


class KernelParameters:
    def __init__(self, *args):
        self.param_list = list(args)

    def get_params_values(self):
        param_list = list()
        for param in self.param_list:
            param_list.append(param.get_value())
        return param_list


class ParameterType(Enum):
    SIMPLE = 1
    POINTER = 2
