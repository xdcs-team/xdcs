import logging
import os

import toml as toml

_config_locations = [
    './xdcs-agent.toml',
    './xdcs-agent.conf',
    '/etc/xdcs/xdcs-agent.toml',
    '/etc/xdcs/xdcs-agent.conf',
]

logger = logging.getLogger(__name__)


class ConfigurationNotFoundException(Exception):
    def __init__(self, *args: object) -> None:
        super().__init__(*args)


class MissingConfigurationException(Exception):
    def __init__(self, *args: object) -> None:
        super().__init__(*args)


def load_config():
    for config_loc in _config_locations:
        logger.debug('Checking location ' + config_loc + ' for configuration')
        if os.path.isfile(config_loc):
            return toml.load(config_loc)
        else:
            logger.debug('Config not found in ' + config_loc)

    raise ConfigurationNotFoundException(
        'Configuration not found, searched locations: ' + str(_config_locations))
