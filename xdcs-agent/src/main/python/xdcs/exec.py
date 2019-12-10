import logging
import subprocess

from xdcs.decorators import asynchronous
from xdcs.log_handling import LogHandler

logger = logging.getLogger(__name__)


class ExecFailedException(Exception):
    pass


def exec_cmd(args: [str], log_handler: LogHandler = None):
    command = ', '.join(args)
    logger.info("Executing: " + command)
    proc = subprocess.Popen(args,
                            stdout=subprocess.PIPE,
                            stderr=subprocess.PIPE)

    if log_handler is not None:
        _consume_stdout(log_handler, proc)
        _consume_stderr(log_handler, proc)

    proc.wait()

    if proc.returncode != 0:
        raise ExecFailedException(str(proc.stderr.read().decode("utf-8")))


@asynchronous
def _consume_stdout(log_handler: LogHandler, proc: subprocess.Popen):
    for line in proc.stdout:
        log_handler.out_bytes(line.strip(b'\n'))


@asynchronous
def _consume_stderr(log_handler: LogHandler, proc: subprocess.Popen):
    for line in proc.stderr:
        log_handler.err_bytes(line.strip(b'\n'))
