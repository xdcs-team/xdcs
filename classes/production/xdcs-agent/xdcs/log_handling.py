from __future__ import annotations

from logging import Logger
from queue import Queue
from typing import Optional

import time

from xdcs.app import xdcs
from xdcs.decorators import asynchronous
from xdcs_api.agent_execution_pb2 import Logs
from xdcs_api.agent_execution_pb2_grpc import TaskReportingStub


class LogHandler:
    def out_bytes(self, message: bytes) -> None:
        pass

    def err_bytes(self, message: bytes) -> None:
        pass

    def combine(self, other: LogHandler) -> LogHandler:
        return CombinedLogHandler(self, other)


class CombinedLogHandler(LogHandler):
    _first: LogHandler
    _second: LogHandler

    def __init__(self, first: LogHandler, second: LogHandler) -> None:
        self._first = first
        self._second = second

    def out_bytes(self, message: bytes) -> None:
        self._first.out_bytes(message)
        self._second.out_bytes(message)

    def err_bytes(self, message: bytes) -> None:
        self._first.err_bytes(message)
        self._second.err_bytes(message)


class PassThroughLogHandler(LogHandler):
    _logger: Logger

    def __init__(self, logger: Logger) -> None:
        self._logger = logger

    def out_bytes(self, message: bytes) -> None:
        self._logger.info("[stdout] " + message.decode('utf-8').rstrip('\n'))

    def err_bytes(self, message: bytes) -> None:
        self._logger.info("[stderr] " + message.decode('utf-8').rstrip('\n'))


class UploadingLogHandler(LogHandler):
    _log_queue: Optional[Queue]
    _task_id: str

    def __init__(self, task_id: str) -> None:
        self._task_id = task_id
        self._log_generator = None
        self._log_queue = Queue()

    def out_bytes(self, message: bytes) -> None:
        line = Logs.LogLine()
        line.timestamp.GetCurrentTime()
        line.contents = message
        line.type = Logs.LogType.STDOUT
        self._log_queue.put(line)

    def err_bytes(self, message: bytes) -> None:
        line = Logs.LogLine()
        line.timestamp.GetCurrentTime()
        line.contents = message
        line.type = Logs.LogType.STDERR
        self._log_queue.put(line)

    def __enter__(self):
        self._send_messages()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self._log_queue.put(None)

    @asynchronous
    def _send_messages(self):
        q = self._log_queue
        stub = TaskReportingStub(xdcs().channel())

        finished = False
        while not finished:
            lines = [q.get()]
            while not q.empty():
                lines.append(q.get())

            if lines[-1] is None:
                lines = lines[0:-1]
                finished = True

            if len(lines) > 0:
                logs = Logs()
                logs.taskId = self._task_id
                logs.lines.extend(lines)
                stub.UploadLogs(logs)
                time.sleep(1)
