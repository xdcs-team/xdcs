from xdcs.app import xdcs
from xdcs.cmd import Command
from xdcs.log_handling import LogHandler
from xdcs_api.agent_execution_pb2 import TaskResultReport
from xdcs_api.agent_execution_pb2_grpc import TaskReportingStub


class ReportTaskCompletionCmd(Command):
    _task_id: str
    _log_handler: LogHandler
    _artifact_root: str

    def __init__(self, task_id: str, log_handler: LogHandler,
                 artifact_root: str = None) -> None:
        self._task_id = task_id
        self._log_handler = log_handler
        self._artifact_root = artifact_root

    def execute(self):
        self._log_handler.internal_log("Reporting task completion: " + self._task_id)

        stub, report = _prepare_reporting_data(self._task_id)
        report.result = TaskResultReport.Result.SUCCEEDED
        if self._artifact_root:
            report.artifactTree = self._artifact_root
        stub.ReportTaskResult(report)


class ReportTaskFailureCmd(Command):
    _task_id: str
    _log_handler: LogHandler

    def __init__(self, task_id: str, log_handler: LogHandler) -> None:
        self._task_id = task_id
        self._log_handler = log_handler

    def execute(self):
        self._log_handler.internal_log("Reporting task failure: " + self._task_id)

        stub, report = _prepare_reporting_data(self._task_id)
        report.result = TaskResultReport.Result.FAILED
        stub.ReportTaskResult(report)


def _prepare_reporting_data(task_id):
    stub = TaskReportingStub(xdcs().channel())
    report = TaskResultReport()
    report.taskId = task_id
    return stub, report
