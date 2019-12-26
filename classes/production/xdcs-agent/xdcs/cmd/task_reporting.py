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

        stub, task_id = _prepare_reporting_data(self._task_id)
        stub.ReportCompletion(task_id)


class ReportTaskFailureCmd(Command):
    _task_id: str

    def __init__(self, task_id: str) -> None:
        self._task_id = task_id

    def execute(self):
        logger.info('Reporting task failure: ' + self._task_id)

        stub, task_id = _prepare_reporting_data(self._task_id)
        stub.ReportFailure(task_id)


def _prepare_reporting_data(task_id):
    stub = TaskReportingStub(xdcs().channel())
    task_identifier = TaskId()
    task_identifier.taskId = task_id
    return stub, task_identifier
