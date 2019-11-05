import fnmatch
import os
import shutil

import sys
from pybuilder.core import use_plugin, init, task, dependents, depends
from pybuilder.plugins.core_plugin import clean, compile_sources
from pybuilder.plugins.python.flake8_plugin import analyze

use_plugin("python.core")
use_plugin("python.unittest")
use_plugin("python.install_dependencies")
use_plugin("python.distutils")
use_plugin("python.pycharm")
use_plugin("python.flake8")
use_plugin("exec")

name = "xdcs-agent"

version = "0.1.0.dev"
default_task = "publish"


@init
def check_version():
    if sys.version_info[0] < 3:
        raise Exception("Only python 3 is supported")


@init
def initialize(project):
    project.build_depends_on('flake8')
    project.build_depends_on('mockito')
    project.build_depends_on('protobuf')
    project.build_depends_on('grpcio')
    project.build_depends_on('grpcio-tools')
    project.build_depends_on('toml')

    project.depends_on('py-cpuinfo')
    project.depends_on('pyopencl')

    # This dependency should be optional
    # as it requires CUDA to be installed.
    # project.depends_on('pycuda')

    project.set_property('distutils_packages', [
        "xdcs"])
    project.set_property('distutils_console_scripts', [
        "xdcs-agent = xdcs.agent_cli:main"])
    project.set_property('flake8_break_build', True)
    project.set_property('flake8_verbose_output', True)


@task
@dependents(compile_sources)
@depends(analyze)
def ensure_analyzed():
    pass


@task
@dependents(compile_sources)
def compile_grpc():
    from grpc_tools import protoc
    import pkg_resources

    gen_python = './target/generated-sources/protobuf/python'

    os.makedirs(gen_python, exist_ok=True)
    proto_include = pkg_resources.resource_filename('grpc_tools', '_proto')
    exit_code = protoc.main([sys.argv[0],
                             '-I=src/main/proto',
                             '--python_out={}'.format(gen_python),
                             '--grpc_python_out={}'.format(gen_python),
                             *find_proto_files('./src/main/proto/xdcs_api/'),
                             '-I{}'.format(proto_include)])

    if exit_code != 0:
        raise Exception('protoc failed ' + str(exit_code))


def find_proto_files(path):
    result = []
    for root, dirs, files in os.walk(path):
        for file in files:
            if fnmatch.fnmatch(file, '*.proto'):
                result.append(os.path.join(root, file))
    return result


@task
@dependents(clean)
def clean_grpc():
    shutil.rmtree('./target/generated-sources/protobuf')


if __name__ == '__main__':
    import pybuilder.cli

    sys.exit(pybuilder.cli.main(*sys.argv[1:]))
