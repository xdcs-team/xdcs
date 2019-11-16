import logging
import re
import subprocess
from typing import Union, List

from lazy import lazy
from packaging import version

from xdcs import gpu
from xdcs.app import xdcs

logger = logging.getLogger(__name__)


class DockerException(Exception):
    pass


class DockerCli:
    _docker_exec: str
    _opts: [str] = []
    _nvidia_all_devices: bool = False
    _nvidia_device_ids: [str] = []

    def __init__(self) -> None:
        self._docker_exec = xdcs().config('system.docker', 'docker')

    def opt(self, opt: Union[str, List[str]]) -> None:
        if isinstance(opt, str):
            self._opts.extend([opt])
        else:
            self._opts.extend(opt)

    def remove_container_after_finish(self) -> None:
        self._opts.extend(['--rm'])

    def container_name(self, name: str) -> None:
        self._opts.extend(['--name', name])

    def nvidia_all_devices(self):
        self._nvidia_all_devices = True

    def nvidia_device(self, key: str) -> None:
        device = gpu.manager.device_by_key(key)
        if not device.is_nvidia():
            raise DockerException('Non-Nvidia devices are not supported by Docker')

        self._nvidia_device_ids.extend(device.nvidia_id())

    def __run_docker(self, args: [str]) -> subprocess.CompletedProcess:
        return subprocess.run([self._docker_exec, *args],
                              stdout=subprocess.PIPE,
                              stderr=subprocess.PIPE,
                              text=True)

    def run(self, image: str) -> None:
        opts = [*self._opts, *self.__build_gpu_opts()]
        proc = subprocess.Popen([self._docker_exec, 'run', *opts, image],
                                stdout=subprocess.PIPE,
                                stderr=subprocess.PIPE)
        for line in proc.stdout:
            logger.info(line.decode("utf-8").rstrip('\n'))

        proc.wait()

        if proc.returncode != 0:
            raise DockerException('Docker run failed: ' + str(proc.stderr.read().decode("utf-8")))

    def build(self, directory, dockerfile) -> str:
        proc = self.__run_docker(['build', '-q', '-f', dockerfile, directory])
        if proc.returncode != 0:
            raise DockerException('Docker build failed: ' + str(proc.stderr))
        return proc.stdout.strip()

    def version(self) -> str:
        proc = self.__run_docker(['--version'])
        if proc.returncode != 0:
            raise DockerException('Docker version retrieval failed: ' + str(proc.stderr))
        return proc.stdout.strip()

    def __build_gpu_opts(self):
        device_list_str = ','.join(self._nvidia_device_ids)

        if info.is_native_gpu_supported():
            if self._nvidia_all_devices:
                return ['--gpus', 'all']

            if self._nvidia_device_ids:
                return ['--gpus', 'device=' + device_list_str]

            return []

        if info.is_native_gpu_supported():
            if self._nvidia_all_devices:
                return ['--runtime=nvidia',
                        '-e', 'NVIDIA_VISIBLE_DEVICES=all']

            if self._nvidia_device_ids:
                return ['--runtime=nvidia',
                        '-e', 'NVIDIA_VISIBLE_DEVICES=' + device_list_str]

            return []

        return []


class _DockerInfo:
    @lazy
    def docker_available(self) -> bool:
        try:
            DockerCli().version()
            return True
        except DockerException:
            return False

    @lazy
    def version(self) -> str:
        docker_version_output = DockerCli().version()
        version_search = re.search('Docker version ([0-9.]+), build ([a-f0-9]+)',
                                   docker_version_output, re.IGNORECASE)

        if version_search:
            return version_search.group(1)
        else:
            raise DockerException('Unrecognized version output: ' + docker_version_output)

    @lazy
    def is_native_gpu_supported(self) -> bool:
        if version.parse(self.version()) < version.parse("19.03"):
            return False

        try:
            DockerCli() \
                .opt(['--gpus', 'all']) \
                .run('hello-world')
            return True
        except DockerException as e:
            if 'could not select device driver' in str(e):
                return False
            raise e

    @lazy
    def is_nvidia_runtime_available(self) -> bool:
        try:
            DockerCli() \
                .opt('--runtime=nvidia') \
                .run('hello-world')
            return True
        except DockerException as e:
            if 'Unknown runtime' in str(e):
                return False
            raise e


info = _DockerInfo()
