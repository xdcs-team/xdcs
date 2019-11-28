import logging

from xdcs.app import xdcs
from xdcs.cmd import Command
from xdcs_api.agent_execution_pb2 import TaskId
from xdcs_api.agent_execution_pb2_grpc import TaskReportingStub

logger = logging.getLogger(__name__)


class ReportTaskCompletionCmd(Command):
    _task_id: str

    def __init__(self, task_id: str) -> None:
        self._task_id = task_id

    def execute(self):
        logger.info('Reporting task completion: ' + self._task_id)

        stub = TaskReportingStub(xdcs().channel())
        task_id = TaskId()
        task_id.taskId = self._task_id
        stub.ReportCompletion(task_id)
