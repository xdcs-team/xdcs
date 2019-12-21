import logging
import subprocess
import threading
from typing import Mapping

from xdcs.decorators import asynchronous
from xdcs.log_handling import LogHandler

logger = logging.getLogger(__name__)


class ExecFailedException(Exception):
    pass


def exec_cmd(args: [str], env: Mapping[str, bytes], log_handler: LogHandler = None, cwd=None):
    command = ', '.join(args)
    logger.info("Executing: " + command)
    proc = subprocess.Popen(args,
                            stdout=subprocess.PIPE,
                            stderr=subprocess.PIPE,
                            env=env,
                            cwd=cwd)

    if log_handler is not None:
        barrier = threading.Barrier(3)
        _consume_stdout(log_handler, proc, barrier)
        _consume_stderr(log_handler, proc, barrier)
    else:
        barrier = threading.Barrier(1)

    proc.wait()
    barrier.wait()

    if proc.returncode != 0:
        raise ExecFailedException(str(proc.stderr.read().decode("utf-8")))


@asynchronous
def _consume_stdout(log_handler: LogHandler, proc: subprocess.Popen, barrier: threading.Barrier):
    for line in proc.stdout:
        log_handler.out_bytes(line.strip(b'\n'))
    barrier.wait()


@asynchronous
def _consume_stderr(log_handler: LogHandler, proc: subprocess.Popen, barrier: threading.Barrier):
    for line in proc.stderr:
        log_handler.err_bytes(line.strip(b'\n'))
    barrier.wait()
