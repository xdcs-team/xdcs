from os import path

from xdcs.cmd.tasks import TaskExecutionException


def get_config_filepath(name: str, workspace_path: str, deployment: dict):
    config_path = deployment['config'].get(name)
    filepath = join_paths(workspace_path, get_relative_path_part(config_path))

    if filepath is None:
        raise TaskExecutionException('%s path is empty' % name)
    return filepath


def join_paths(prefix, suffix):
    filepath = path.join(prefix, get_relative_path_part(suffix))
    filepath = path.normpath(filepath)
    if not filepath.startswith(prefix):
        raise TaskExecutionException('Path traversal detected')
    return filepath


def get_relative_path_part(filepath: str):
    if filepath.startswith('/'):
        return filepath[1:]
    return filepath
