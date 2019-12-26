import logging
from concurrent.futures import Executor, ThreadPoolExecutor

import grpc
from grpc._interceptor import _Channel

from xdcs import object_repository
from xdcs.cmd import Command
from xdcs.config import load_config, MissingConfigurationException
from xdcs.object_repository import ObjectRepository
from xdcs.servicers.Servicers import Servicers
from xdcs.utils.rforward import rforward

logger = logging.getLogger(__name__)


class _XDCS:
    _NO_ARG = object()

    _token: str
    _channel: _Channel
    _executor: Executor
    _config: dict
    _or: ObjectRepository

    _fs_repo_path: str
    _or_path: str

    def __init__(self, config_location=None) -> None:
        self._config = load_config(config_location)
        self._executor = ThreadPoolExecutor(max_workers=self.config('app.executors', 10))
        self._fs_repo_path = self.config('app.fs_repo_path', './data')
        self._or_path = self._fs_repo_path + '/objects'
        self._or = object_repository.from_path(self._or_path)
        self.__generate_rsa_keys_if_needed()

    def executor(self) -> Executor:
        return self._executor

    def execute(self, command: Command) -> None:
        command.execute()

    def run(self) -> None:
        local_port = self.config('app.local_port', 25254)
        server_host = self.config('server.host')
        server_port = self.config('server.port.ssh')

        server = grpc.server(self.executor())
        Servicers.register_all(server)
        server.add_insecure_port('0.0.0.0:' + str(local_port))
        server.start()
        with rforward(local_port, server_host, server_port, False):
            pass

    def set_token(self, token: str) -> None:
        self._token = token

    def token(self) -> str:
        return self._token

    def set_channel(self, channel) -> None:
        self._channel = channel

    def object_repository(self) -> ObjectRepository:
        return self._or

    def channel(self):
        return self._channel

    def config(self, prop: str, default_value=_NO_ARG):
        props = prop.split('.')
        current_config = self._config

        for p in props:
            if p not in current_config:
                if default_value == self._NO_ARG:
                    raise MissingConfigurationException('Required config: ' + prop)

                return default_value

            current_config = current_config[p]

        return current_config

    def __generate_rsa_keys_if_needed(self):
        if not self.config('server.auth.generate_keys', False):
            return

        import paramiko
        import os
        private_file_path: str = self.config('server.auth.key', None)

        if private_file_path is None:
            logger.info('Not generating keys, because no key path has been configured')
            return

        public_file_path: str = private_file_path + '.pub'

        os.makedirs(os.path.dirname(private_file_path), exist_ok=True)

        logger.info("Generating RSA keys...")

        key = paramiko.rsakey.RSAKey.generate(4096)

        key.write_private_key_file(private_file_path)
        os.chmod(private_file_path, 0o0600)

        with open(public_file_path, 'w') as content_file:
            content_file.write('ssh-rsa ')
            content_file.write(key.get_base64())
            content_file.write('\n')

        logger.info("Keys generated: {}, {}".format(private_file_path, public_file_path))
