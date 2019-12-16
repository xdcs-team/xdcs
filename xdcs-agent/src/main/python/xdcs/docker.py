from __future__ import annotations

import logging
import os
import re
import subprocess
import tempfile
from typing import Union, List

from packaging import version

from xdcs import gpu
from xdcs.app import xdcs
from xdcs.decorators import lazy
from xdcs.exec import exec_cmd, ExecFailedException
from xdcs.log_handling import LogHandler

logger = logging.getLogger(__name__)


class DockerException(Exception):
    pass


class DockerCli:
    _docker_exec: str
    _opts: [str]
    _nvidia_all_devices: bool
    _nvidia_device_ids: [str]

    def __init__(self) -> None:
        self._docker_exec = xdcs().config('system.docker', 'docker')
        self._opts = []
        self._nvidia_all_devices = False
        self._nvidia_device_ids = []

    def opt(self, opt: Union[str, List[str]]) -> DockerCli:
        if isinstance(opt, str):
            self._opts.extend([opt])
        else:
            self._opts.extend(opt)

        return self

    def with_env_variable(self, name: str, value: str):
        self._opts.extend(['-e', name + '=' + value])
        return self

    def allocate_pseudo_tty(self) -> DockerCli:
        self._opts.extend(['-t'])
        return self

    def remove_container_after_finish(self) -> DockerCli:
        self._opts.extend(['--rm'])
        return self

    def container_name(self, name: str) -> DockerCli:
        self._opts.extend(['--name', name])
        return self

    def nvidia_all_devices(self) -> DockerCli:
        self._nvidia_all_devices = True
        return self

    def nvidia_device(self, key: str) -> DockerCli:
        device = gpu.manager.device_by_key(key)
        if not device.is_nvidia():
            raise DockerException('Non-Nvidia devices are not supported by Docker')

        self._nvidia_device_ids.extend(device.nvidia_id())
        return self

    def __run_docker(self, args: [str]) -> subprocess.CompletedProcess:
        return subprocess.run([self._docker_exec, *args],
                              stdout=subprocess.PIPE,
                              stderr=subprocess.PIPE,
                              text=True)

    def run(self, image: str, log_handler: LogHandler = None) -> str:
        with tempfile.TemporaryDirectory() as tmpdir:
            cid_file = os.path.join(tmpdir, 'cid')
            opts = [*self._opts, *self.__build_gpu_opts(), '--cidfile', cid_file]
            try:
                exec_cmd([self._docker_exec, 'run', *opts, image], dict(os.environ), log_handler)

                with open(cid_file, 'r') as cidf:
                    return cidf.read()
            except ExecFailedException as e:
                raise DockerException('Docker run failed', e)

    def build(self, directory, dockerfile) -> str:
        proc = self.__run_docker(['build', '-q', '-f', dockerfile, directory])
        if proc.returncode != 0:
            raise DockerException('Docker build failed: ' + str(proc.stderr))
        return proc.stdout.strip()

    def rm(self, image: str) -> None:
        proc = self.__run_docker(['rm', image])
        if proc.returncode != 0:
            raise DockerException('Docker image removal failed: ' + str(proc.stderr))

    def cp(self, cid: str, src_path: str, dst_path: str) -> None:
        proc = self.__run_docker(['cp', cid + ':' + src_path, dst_path])
        if proc.returncode != 0:
            raise DockerException('Docker cp failed: ' + str(proc.stderr))

    def version(self) -> str:
        proc = self.__run_docker(['--version'])
        if proc.returncode != 0:
            raise DockerException('Docker version retrieval failed: ' + str(proc.stderr))
        return proc.stdout.strip()

    def __build_gpu_opts(self):
        device_list_str = ','.join(self._nvidia_device_ids)

        if not self._nvidia_all_devices and not self._nvidia_device_ids:
            return []

        if info.is_native_gpu_supported():
            if self._nvidia_all_devices:
                return ['--gpus', 'all']

            if self._nvidia_device_ids:
                return ['--gpus', 'device=' + device_list_str]

            return []

        if info.is_nvidia_runtime_available():
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
        version_search = re.search('Docker version ([0-9.]+)(.*)',
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
                .remove_container_after_finish() \
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
                .remove_container_after_finish() \
                .opt('--runtime=nvidia') \
                .run('hello-world')
            return True
        except DockerException as e:
            if 'Unknown runtime' in str(e):
                return False
            raise e


info = _DockerInfo()
