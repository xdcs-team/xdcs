import importlib
import os
import platform
import sys

import cpuinfo
import distro
import pyopencl
import pycuda.autoinit

from xdcs import docker
from xdcs.app import xdcs
from xdcs_api.agent_info_pb2 import ProcessorInfo, SystemInfo, GPUInfo, OpenCLPlatform, OpenCLDevice, SoftwareInfo, \
    CUDAInfo, CUDADevice
from xdcs_api.agent_info_pb2_grpc import AgentInfoServicer

pycuda.autoinit


class AgentInfo(AgentInfoServicer):

    def GetProcessorInfo(self, request, context) -> ProcessorInfo:
        cpu_info = cpuinfo.get_cpu_info()

        processor_info = ProcessorInfo()

        processor_info.cpu_count = os.cpu_count()
        processor_info.cpu_name = platform.processor()
        processor_info.cpu_machine = platform.machine()
        processor_info.cpu_arch = cpu_info['arch']
        processor_info.cpu_bits = cpu_info['bits']
        processor_info.cpu_vendor_id = cpu_info['vendor_id']
        processor_info.cpu_brand = cpu_info['brand']
        processor_info.cpu_clock = cpu_info['hz_advertised']
        processor_info.cpu_clock_actual = cpu_info['hz_actual']
        processor_info.cpu_flags[:] = cpu_info['flags']

        processor_info.cpu_cache_l1_instr = cpu_info['l1_instruction_cache_size']
        processor_info.cpu_cache_l1 = cpu_info['l1_data_cache_size']
        processor_info.cpu_cache_l2 = cpu_info['l2_cache_size']
        processor_info.cpu_cache_l3 = cpu_info['l3_cache_size']

        return processor_info

    def GetSystemInfo(self, request, context) -> SystemInfo:
        system_info = SystemInfo()

        system_info.python_version.major = sys.version_info.major
        system_info.python_version.minor = sys.version_info.minor
        system_info.python_version.micro = sys.version_info.micro
        system_info.python_version.releaselevel = sys.version_info.releaselevel
        system_info.python_version.serial = sys.version_info.serial
        system_info.python_version.version_str = sys.version

        system_info.system_family = os.name
        system_info.system_family_name = platform.system()
        system_info.platform = platform.platform()
        for name, value in os.environ.items():
            system_info.env[name] = value
        system_info.uname_sysname = platform.system()
        system_info.uname_nodename = platform.node()
        system_info.uname_release = platform.release()
        system_info.uname_version = platform.version()
        system_info.uname_machine = platform.machine()
        system_info.dist_name = distro.name()
        system_info.dist_desc = distro.name(True)
        system_info.dist_version = distro.version()
        system_info.dist_id = distro.id()

        return system_info

    def GetSoftwareInfo(self, request, context) -> SoftwareInfo:
        software_info = SoftwareInfo()
        if xdcs().config('app.send_path_executables'):
            software_info.programs.extend(self._get_programs_from_path())
        software_info.dockerVersion = docker.info.version()

        return software_info

    def _get_programs_from_path(self):
        paths = os.environ["PATH"].split(":")
        programs = []

        for path in paths:
            if not os.path.isdir(path):
                continue

            for file in os.listdir(path):
                full_path = os.path.join(path, file)
                is_file = os.path.isfile(full_path)
                is_executable = os.access(full_path, os.X_OK)
                if is_file and is_executable:
                    programs.append(full_path)

        return programs

    def GetGPUInfo(self, request, context) -> GPUInfo:
        platforms = pyopencl.get_platforms()

        gpu_info = GPUInfo()
        for pfm in platforms:
            self._map_platform(gpu_info.opencl_platforms.add(), pfm)
        gpu_info.cuda_info.CopyFrom(self._get_cuda_info())

        return gpu_info

    def _map_platform(self, mapped: OpenCLPlatform, cl_platform) -> None:
        mapped.name = cl_platform.name
        mapped.profile = cl_platform.profile
        mapped.vendor = cl_platform.vendor
        mapped.version = cl_platform.version
        mapped.extensions[:] = cl_platform.extensions.strip().split(' ')
        for device in cl_platform.get_devices(pyopencl.device_type.ALL):
            self._map_device(mapped.devices.add(), device)

    def _map_device(self, mapped: OpenCLDevice, device) -> None:
        mapped.version = device.version
        mapped.type = pyopencl.device_type.to_string(device.type)
        mapped.extensions[:] = device.extensions.strip().split(' ')
        mapped.global_memory = device.global_mem_size
        mapped.local_memory = device.local_mem_size
        mapped.address_bits = device.address_bits
        mapped.max_work_item_dims = device.max_work_item_dimensions
        mapped.max_work_group_size = device.max_work_group_size
        mapped.max_compute_units = device.max_compute_units
        mapped.driver_version = device.driver_version

    def _get_cuda_info(self):
        if importlib.util.find_spec("pycuda") is None:
            return CUDAInfo(cuda_available=False)

        import pycuda
        from pycuda.driver import Device

        cuda_info = CUDAInfo()

        driver_version = pycuda.driver.get_driver_version()

        if driver_version == 0:
            return CUDAInfo(cuda_available=False)

        cuda_info.cuda_available = True
        (cuda_info.driver_version.major,
         cuda_info.driver_version.minor,
         cuda_info.driver_version.revision) = (
            int(driver_version / 1000),
            int(driver_version / 10 % 100),
            int(driver_version % 10))

        cuda_info.pycuda_version_text = pycuda.VERSION_TEXT
        (cuda_info.pycuda_cuda_version.major,
         cuda_info.pycuda_cuda_version.minor,
         cuda_info.pycuda_cuda_version.revision) = pycuda.driver.get_version()

        for device_id in range(0, Device.count()):
            self._map_cuda_device(cuda_info.devices.add(), device_id)

        return cuda_info

    def _map_cuda_device(self, mapped: CUDADevice, device_id):
        import pycuda
        from pycuda.driver import Device

        dev = Device(device_id)
        ctx = dev.make_context()

        try:
            mapped.name = dev.name()
            mapped.pci_bus_id = dev.pci_bus_id()
            mapped.pci_device_id = dev.pci_device_id
            mapped.pci_domain_id = dev.pci_domain_id
            mapped.compute_capability_major = dev.compute_capability_major
            mapped.compute_capability_minor = dev.compute_capability_minor
            (free_mem, total_mem) = pycuda.driver.mem_get_info()
            mapped.free_memory = free_mem
            mapped.total_memory = total_mem
            mapped.max_threads_per_block = dev.max_threads_per_block
            mapped.max_block_dim_x = dev.max_block_dim_x
            mapped.max_block_dim_y = dev.max_block_dim_y
            mapped.max_block_dim_z = dev.max_block_dim_z
            mapped.max_grid_dim_x = dev.max_grid_dim_x
            mapped.max_grid_dim_y = dev.max_grid_dim_y
            mapped.max_grid_dim_z = dev.max_grid_dim_z
            mapped.clock_rate = dev.clock_rate
            mapped.multiprocessor_count = dev.multiprocessor_count
            mapped.shared_memory_per_block = dev.shared_memory_per_block
            mapped.total_constant_memory = dev.total_constant_memory
            mapped.integrated = dev.integrated
            mapped.concurrent_kernels = dev.concurrent_kernels
            mapped.uses_tcc = dev.tcc_driver
            mapped.mem_clock_rate = dev.memory_clock_rate
            mapped.mem_bus_width = dev.global_memory_bus_width
            mapped.l2_cache_size = dev.l2_cache_size
            mapped.max_threads_per_multiprocessor = dev.max_threads_per_multiprocessor
            mapped.async_engine_count = dev.async_engine_count
        finally:
            ctx.pop()
