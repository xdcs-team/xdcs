import logging

from xdcs.app import xdcs

logger = logging.getLogger(__name__)


def asynchronous(func):
    def task(*args, **kwargs):
        try:
            func(*args, **kwargs)
        except Exception as e:
            logger.exception('Exception during asynchronous execution: ' + str(e))
            raise e

    def replacement(*args, **kwargs):
        return xdcs().executor() \
            .submit(lambda: task(*args, **kwargs))

    return replacement
