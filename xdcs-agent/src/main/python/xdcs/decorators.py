from xdcs.app import xdcs


def asynchronous(func):
    def replacement(*args, **kwargs):
        xdcs().executor()\
            .submit(lambda: func(*args, **kwargs))

    return replacement
