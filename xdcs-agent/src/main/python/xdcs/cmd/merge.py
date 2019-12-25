import os
import tempfile
from os import path
from typing import Optional

from xdcs.app import xdcs
from xdcs.cmd import Command
from xdcs.cmd.object_repository import FetchDeploymentWithDependenciesCmd, DumpObjectRepositoryTreeCmd,\
    UploadObjectsCmd, MaterializeTreeToObjectRepositoryCmd, FetchTreeWithDependenciesCmd
from xdcs.cmd.task_reporting import ReportTaskCompletionCmd
from xdcs.exec import exec_cmd
from xdcs.log_handling import LogHandler
from xdcs.utils import deploymentutils


class MergeTaskCmd(Command):
    _deployment_id: str
    _task_id: str
    _artifact_trees: [str]
    _log_handler: LogHandler

    def __init__(self, deployment_id: str, task_id: str, artifact_trees: [str], log_handler: LogHandler) -> None:
        self._deployment_id = deployment_id
        self._task_id = task_id
        self._artifact_trees = artifact_trees
        self._log_handler = log_handler

    def execute(self):
        log_handler = self._log_handler
        with tempfile.TemporaryDirectory() as merging_area:
            xdcs().execute(FetchDeploymentWithDependenciesCmd(self._deployment_id))
            deployment: dict = xdcs().object_repository().cat_json(self._deployment_id)
            root_id = deployment['root']
            workspace_path = path.join(merging_area, 'workspace')
            os.makedirs(workspace_path, exist_ok=True)
            xdcs().execute(DumpObjectRepositoryTreeCmd(root_id, workspace_path))
            merging_script_path = deploymentutils.get_config_filepath('mergingscript', workspace_path, deployment)

            args = [merging_script_path]
            tree_paths = self.fetch_argument_trees(merging_area)
            args.extend(tree_paths)

            with tempfile.TemporaryDirectory() as results_root:
                exec_cmd(args, None, self._log_handler, cwd=results_root)
                artifact_root = self._gather_artifacts(results_root)

            xdcs().execute(ReportTaskCompletionCmd(self._task_id, self._log_handler, artifact_root))
            log_handler.internal_log("Deployment finished: " + self._deployment_id)

    def fetch_argument_trees(self, workspace_path: str):
        tree_paths = []
        for artifact_tree in self._artifact_trees:
            xdcs().execute(FetchTreeWithDependenciesCmd(artifact_tree))
            tree_path = deploymentutils.join_paths(workspace_path, artifact_tree)
            os.makedirs(tree_path, exist_ok=True)
            xdcs().execute(DumpObjectRepositoryTreeCmd(artifact_tree, tree_path))
            tree_paths.append(tree_path)
        return tree_paths

    @staticmethod
    def _gather_artifacts(results_root) -> Optional[str]:
        root_id, all_objects = MaterializeTreeToObjectRepositoryCmd(results_root).execute()
        UploadObjectsCmd(all_objects).execute()
        return root_id
