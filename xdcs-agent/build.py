import fnmatch
import os
import shutil
import sys

from pybuilder.core import use_plugin, init, task, dependents, depends, Author
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
version = "0.1.0"
default_task = "publish"
long_description = "XDCS Distributed Computing Software"
authors = [Author('Kamil Jarosz', 'kjarosz@student.agh.edu.pl'),
           Author('Krystian Zycinski', 'zycinski@student.agh.edu.pl'),
           Author('Adam Szczerba', 'adamszczerba19@gmail.com'),
           Author('Jan Rodzon', 'rodzonjan@wp.pl')]
url = 'https://github.com/xdcs-team/xdcs'

@init
def check_version():
    if sys.version_info[0] < 3:
        raise Exception("Only python 3 is supported")


@init
def initialize(project):
    project.build_depends_on('flake8')
    project.build_depends_on('mockito')
    project.build_depends_on('grpcio-tools')

    project.depends_on('paramiko')
    project.depends_on('protobuf')
    # TODO: Get rid of this dependency if possible
    project.depends_on('numpy')
    project.depends_on('toml')
    project.depends_on('grpcio')
    project.depends_on('py-cpuinfo')
    project.depends_on('pyopencl')
    project.depends_on('distro')
    project.depends_on('packaging')

    # This dependency should be optional
    # as it requires CUDA to be installed.
    # project.depends_on('pycuda')

    project.set_property('distutils_packages', [
        "xdcs"])
    project.set_property('distutils_console_scripts', [
        "xdcs-agent = xdcs.agent_cli:main"])
    project.set_property('flake8_break_build', True)
    project.set_property('flake8_verbose_output', True)
    project.install_file("/etc/xdcs", "xdcs/conf/xdcs-agent.toml")
    project.install_file("/lib/systemd/system/", "xdcs/conf/xdcs-agent.service")


@task
@dependents(compile_sources)
@depends(analyze)
def ensure_analyzed():
    pass


@task
@dependents(compile_sources)
def compile_grpc(logger):
    from grpc_tools import protoc
    import pkg_resources

    gen_python = './target/generated-sources/protobuf/python'

    os.makedirs(gen_python, exist_ok=True)
    proto_include = pkg_resources.resource_filename('grpc_tools', '_proto')
    params = [sys.argv[0],
              '-I=src/main/proto',
              '--python_out={}'.format(gen_python),
              '--grpc_python_out={}'.format(gen_python),
              *find_proto_files('./src/main/proto/xdcs_api/'),
              '-I{}'.format(proto_include)]
    logger.debug("Executing protoc with parameters {}".format(params))
    exit_code = protoc.main(params)

    if exit_code != 0:
        raise Exception('protoc failed ' + str(exit_code))
    else:
        logger.info("protoc succeeded")
        for subdir, _, _ in os.walk(gen_python):
            path = subdir + '/__init__.py'
            with open(path, "w+"):
                pass
            logger.debug(path + ' generated')


def find_proto_files(path):
    result = []
    for subdir, _, files in os.walk(path):
        for file in files:
            if fnmatch.fnmatch(file, '*.proto'):
                result.append(os.path.join(subdir, file))
    return result


@task
@dependents(clean)
def clean_grpc():
    shutil.rmtree('./target/generated-sources/protobuf')


if __name__ == '__main__':
    import pybuilder.cli

    sys.exit(pybuilder.cli.main(*sys.argv[1:]))
