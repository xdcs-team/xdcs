import os

from pybuilder.core import use_plugin, init, task, dependents
from pybuilder.plugins.core_plugin import clean

use_plugin("python.install_dependencies")
use_plugin("python.distutils")
use_plugin("exec")

name = "xdcs-agent-api"

version = '0.1.0.dev'
default_task = ["analyze", "publish"]


@init
def initialize(project):
    project.build_depends_on('protobuf')
    project.set_property('dir_source_main_python', 'target/generated-sources/protobuf/python')
    os.makedirs('./target/generated-sources/protobuf/python')
    os.makedirs('./target/generated-sources/protobuf/grpc-python')
    project.set_property(
        'package_command',
        'python3 -m grpc_tools.protoc -I=src/main/proto '
        '--python_out=./target/generated-sources/protobuf/python '
        '--grpc_python_out=./target/generated-sources/protobuf/grpc-python '
        './src/main/proto/agent_api.proto')
    project.set_property('clean_command', 'rm -r ./target/generated-sources/protobuf')


@task
@dependents(clean)
def deploy_unittests_resources(project):
    os.removedirs('./target/generated-sources/protobuf/grpc-python')
    pass
