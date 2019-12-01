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


def lazy(func):
    class Lazy:
        def __init__(self, original) -> None:
            self._value_computed = False
            self._value = None
            self._original = [original]

        def get_value(self, *args, **kwargs):
            if self._value_computed:
                return self._value
            else:
                self._value = func(*args, **kwargs)
                self._value_computed = True
                return self._value

    _lazy = Lazy(func)

    def replacement(*args, **kwargs):
        return _lazy.get_value(*args, **kwargs)

    return replacement
