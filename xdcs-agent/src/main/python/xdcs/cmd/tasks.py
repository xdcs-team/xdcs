import os
import shutil
import tempfile
from typing import Optional

from xdcs.kernel import KernelManager
from xdcs.utils import deploymentutils
from xdcs_api.agent_execution_pb2 import KernelConfig
from xdcs.app import xdcs
from xdcs.cmd import Command
from xdcs.cmd.object_repository import FetchDeploymentWithDependenciesCmd, DumpObjectRepositoryTreeCmd, \
    MaterializeTreeToObjectRepositoryCmd, UploadObjectsCmd
from xdcs.cmd.task_reporting import ReportTaskCompletionCmd
from xdcs.docker import DockerCli
from xdcs.exec import exec_cmd
from xdcs.log_handling import LogHandler, LogLevel
from xdcs_api.agent_execution_pb2 import TaskSubmit


class TaskExecutionException(Exception):
    pass


class RunTaskCmd(Command):
    _deployment_id: str
    _task_id: str
    _agent_variables: TaskSubmit.AgentVariables
    _kernel_config: KernelConfig
    _log_handler: LogHandler

    def __init__(self, deployment_id: str, task_id: str, agent_variables: TaskSubmit.AgentVariables,
                 kernel_config: KernelConfig, log_handler: LogHandler) -> None:
        self._deployment_id = deployment_id
        self._task_id = task_id
        self._agent_variables = agent_variables
        self._kernel_config = kernel_config
        self._log_handler = log_handler

    def execute(self):
        log_handler = self._log_handler
        with tempfile.TemporaryDirectory() as workspace_path:
            xdcs().execute(FetchDeploymentWithDependenciesCmd(self._deployment_id))
            deployment: dict = xdcs().object_repository().cat_json(self._deployment_id)
            root_id = deployment['root']
            xdcs().execute(DumpObjectRepositoryTreeCmd(root_id, workspace_path))
            config_type = deployment['config']['type']
            agent_env_variables = RunTaskCmd.prepare_agent_env_variables(self._agent_variables)

            log_handler.internal_log('Agent variables:', LogLevel.DEBUG)
            for key, value in agent_env_variables.items():
                log_handler.internal_log('    {}={}'.format(key, value), LogLevel.DEBUG)

            constructor_args = [workspace_path, deployment, self._deployment_id, self._task_id, agent_env_variables,
                                log_handler]
            if config_type == 'docker':
                xdcs().execute(RunDockerTaskCmd(*constructor_args))
            elif config_type == 'script':
                xdcs().execute(RunScriptTaskCmd(*constructor_args))
            elif config_type == 'opencl':
                xdcs().execute(RunOpenClTaskCmd(*constructor_args, self._kernel_config))
            else:
                raise Exception('Unsupported task type ' + config_type)
            log_handler.internal_log("Deployment finished: " + self._deployment_id)

    @staticmethod
    def prepare_agent_env_variables(agent_variables: TaskSubmit.AgentVariables) -> dict:
        agent_env_variables: dict = {
            'XDCS_AGENT_IPS': ','.join(agent_variables.agentIps),
            'XDCS_AGENT_IP_MINE': agent_variables.agentIpMine,
            'XDCS_AGENT_COUNT': str(agent_variables.agentCount),
            'XDCS_AGENT_ID': str(agent_variables.agentId)
        }
        for v in agent_variables.environmentVariables:
            agent_env_variables[str(v.name)] = str(v.value)
        if len(agent_variables.agentIps) != agent_variables.agentCount:
            raise Exception('Inconsistent arguments: agent_count = %d, but number of received IPs = %d'
                            % (agent_variables.agentCount, len(agent_variables.agentIps)))

        for agent_id in range(agent_variables.agentCount):
            agent_env_variables['XDCS_AGENT_IP_%d' % agent_id] = agent_variables.agentIps[agent_id]

        return agent_env_variables


class _RunDeploymentBasedTaskCmd(Command):
    _task_id: str
    _deployment_id: str
    _deployment: dict
    _workspace_path: str
    _agent_env_variables: dict
    _log_handler: LogHandler

    def __init__(self, workspace_path: str, deployment: dict, deployment_id: str, task_id: str,
                 agent_env_variables: dict, log_handler: LogHandler) -> None:
        self._workspace_path = workspace_path
        self._deployment_id = deployment_id
        self._deployment = deployment
        self._task_id = task_id
        self._agent_env_variables = agent_env_variables
        self._log_handler = log_handler

    def get_config_filepath(self, name: str):
        return deploymentutils.get_config_filepath(name, self._workspace_path, self._deployment)


class RunDockerTaskCmd(_RunDeploymentBasedTaskCmd):
    def execute(self):
        dockerfile = self.get_config_filepath('dockerfile')
        image_id = DockerCli().build(self._workspace_path, dockerfile)
        self._log_handler.internal_log('Docker built, image ID: ' + image_id)

        artifact_root = self._run_image(image_id)
        xdcs().execute(ReportTaskCompletionCmd(self._task_id, self._log_handler, artifact_root))

    def _run_image(self, image_id: str) -> Optional[str]:
        should_allocate_pseudo_tty = self._deployment['config'].get('allocate-tty', True)

        with tempfile.TemporaryDirectory() as artifacts_root:
            cid = None
            try:
                docker_cli = DockerCli()
                if should_allocate_pseudo_tty:
                    docker_cli.allocate_pseudo_tty()
                for name, value in self._agent_env_variables.items():
                    docker_cli.with_env_variable(name, value)

                # TODO Container name ending with _XDCS_AGENT_ID is only for development purposes.
                #   This should be removed before production release.
                cid = docker_cli \
                    .nvidia_all_devices() \
                    .container_name('xdcs_' + self._task_id + '_%s' % self._agent_env_variables['XDCS_AGENT_ID']) \
                    .run(image_id, self._log_handler)

                artifact_root = self._handle_artifacts(cid, artifacts_root)
                return artifact_root
            finally:
                if cid:
                    DockerCli().rm(cid)

    def _handle_artifacts(self, cid: str, artifacts_root) -> Optional[str]:
        artifacts = self._deployment['config'].get('artifacts', [])
        if len(artifacts) == 0:
            return None

        for artifact in artifacts:
            dest = deploymentutils.join_paths(artifacts_root, artifact)
            DockerCli().cp(cid, artifact, dest)

        root_id, all_objects = MaterializeTreeToObjectRepositoryCmd(artifacts_root).execute()
        UploadObjectsCmd(all_objects).execute()
        return root_id


class RunScriptTaskCmd(_RunDeploymentBasedTaskCmd):
    def execute(self):
        script_path = self.get_config_filepath('scriptfile')

        env = dict(os.environ)
        env.update(self._agent_env_variables)

        exec_cmd([script_path], env, self._log_handler, cwd=self._workspace_path)

        artifact_root = self._gather_artifacts()
        xdcs().execute(ReportTaskCompletionCmd(self._task_id, self._log_handler, artifact_root))

    def _gather_artifacts(self) -> Optional[str]:
        artifacts = self._deployment['config'].get('artifacts', [])
        if len(artifacts) == 0:
            return None

        with tempfile.TemporaryDirectory() as artifacts_root:
            for artifact in artifacts:
                dest = deploymentutils.join_paths(artifacts_root, artifact)
                artifact_path = deploymentutils.join_paths(self._workspace_path, artifact)
                os.makedirs(os.path.dirname(dest), exist_ok=True)
                shutil.copy(artifact_path, dest)

            root_id, all_objects = MaterializeTreeToObjectRepositoryCmd(artifacts_root).execute()
            UploadObjectsCmd(all_objects).execute()
            return root_id


class RunOpenClTaskCmd(_RunDeploymentBasedTaskCmd):
    _kernel_manager: KernelManager

    def __init__(self, workspace_path: str, deployment: dict, deployment_id: str, task_id: str,
                 agent_env_variables: dict, log_handler: LogHandler, kernel_config: KernelConfig) -> None:
        _RunDeploymentBasedTaskCmd.__init__(self, workspace_path, deployment, deployment_id, task_id,
                                            agent_env_variables, log_handler)
        self._kernel_manager = KernelManager.from_config(kernel_config, deployment['config']['kernelparams'])

    def execute(self):
        kernel_path = self.get_config_filepath('kernelfile')

        with open(kernel_path) as kernel_file:
            kernel_program: str = kernel_file.read()
        kernel_name = self._deployment['config']['kernelname']

        self._kernel_manager.prepare_execution(self._agent_env_variables)
        self._kernel_manager.execute_kernel(kernel_program, kernel_name)
        return_arguments = self._kernel_manager.read_output_arguments()

        artifact_root = self._gather_artifacts(return_arguments)
        xdcs().execute(ReportTaskCompletionCmd(self._task_id, self._log_handler, artifact_root))

    def _gather_artifacts(self, return_arguments: [tuple]) -> Optional[str]:
        if len(return_arguments) == 0:
            return None
        with tempfile.TemporaryDirectory() as artifacts_root:
            for name, buffer in return_arguments:
                dest = deploymentutils.join_paths(artifacts_root, name)
                artifact_path = deploymentutils.join_paths(self._workspace_path, name)
                with open(artifact_path, "w+b") as artifact:
                    artifact.write(bytes(buffer))
                os.makedirs(os.path.dirname(dest), exist_ok=True)
                shutil.copy(artifact_path, dest)

            root_id, all_objects = MaterializeTreeToObjectRepositoryCmd(artifacts_root).execute()
            UploadObjectsCmd(all_objects).execute()
            return root_id
