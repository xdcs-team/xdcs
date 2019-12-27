import importlib
import logging
from typing import Dict, Optional

import pyopencl

logger = logging.getLogger(__name__)


class Resource:
    def platform_id(self) -> int:
        return 0

    def device_id(self) -> int:
        pass

    def is_nvidia(self) -> bool:
        pass

    def is_cpu(self) -> bool:
        pass

    def key(self) -> str:
        pass


class _OpenCLResource(Resource):
    _platform_id: int
    _device_id: int
    _key: str

    def __init__(self,
                 platform_id: int,
                 device_id: int) -> None:
        self._platform_id = platform_id
        self._device_id = device_id
        self._key = '/opencl/' + str(platform_id) + '/device/' + str(device_id)

    def platform_id(self) -> int:
        return self._platform_id

    def device_id(self) -> int:
        return self._device_id

    def is_nvidia(self) -> bool:
        return False

    def is_cpu(self) -> bool:
        return False

    def key(self) -> str:
        return self._key


class _CUDAResource(Resource):
    _id: int
    _key: str

    def __init__(self,
                 device_id: int,
                 bus_id: str) -> None:
        self._id = device_id
        self._key = '/nvidia/' + str(device_id)

    def device_id(self) -> int:
        return self._id

    def is_nvidia(self) -> bool:
        return True

    def is_cpu(self) -> bool:
        return False

    def key(self) -> str:
        return self._key


class _CPUResource(Resource):
    def device_id(self) -> int:
        return 0

    def is_nvidia(self) -> bool:
        return False

    def is_cpu(self) -> bool:
        return True

    def key(self) -> str:
        return '/cpu'


def _gather_resources():
    ret = [_CPUResource()]
    ret += _gather_cuda_resources()
    ret += _gather_opencl_resources()
    return ret


def _gather_cuda_resources() -> [Resource]:
    if importlib.util.find_spec("pycuda") is None:
        return []

    import pycuda
    try:
        import pycuda.autoinit
        pycuda.autoinit
        from pycuda.driver import Device

        ret = []
        for device_id in range(0, Device.count()):
            ret += [_gather_cuda_resource(device_id)]

        return ret
    except pycuda._driver.Error as e:
        logger.error('Error while gathering resources, assuming no CUDA devices are present')
        logger.error(e)
        return []


def _gather_cuda_resource(device_id: int) -> Resource:
    from pycuda.driver import Device

    dev = Device(device_id)
    ctx = dev.make_context()

    try:
        return _CUDAResource(
                device_id=device_id,
                bus_id=dev.pci_bus_id()
        )
    finally:
        ctx.pop()


def _gather_opencl_resources() -> [Resource]:
    try:
        platforms = pyopencl.get_platforms()

        ret = []
        for platform_id, platform in enumerate(platforms):
            devices = platform.get_devices(pyopencl.device_type.ALL)
            for device_id, device in enumerate(devices):
                ret += [_gather_opencl_resource(platform_id, device_id, device)]

        return ret
    except pyopencl._cl.LogicError:
        logger.warning("Error while gathering OpenCL devices information, assuming no OpenCL devices found")
        return []


def _gather_opencl_resource(
        platform_id: int, device_id: int, device: pyopencl.Device) -> Resource:
    try:
        pci_bus_id_nv = device.pci_bus_id_nv
    except pyopencl._cl.LogicError:
        pci_bus_id_nv = None
    # TODO pci_bus_id_nv is the bus_id of the nvidia device
    pci_bus_id_nv

    return _OpenCLResource(
            platform_id=platform_id,
            device_id=device_id
    )


class _ResourceManager:
    _resources_by_key: Dict[str, Resource]

    def __init__(self) -> None:
        self._resources_by_key = dict()
        resources = _gather_resources()
        for resource in resources:
            self._resources_by_key[resource.key()] = resource

    def resource_by_key(self, key: str) -> Optional[Resource]:
        return self._resources_by_key[key]

    def all_resources(self) -> [Resource]:
        return self._resources_by_key.values()


manager = _ResourceManager()
