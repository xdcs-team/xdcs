_xdcs = None


def xdcs(*args, **kwargs):
    global _xdcs
    if not _xdcs:
        from xdcs.xdcs import _XDCS
        _xdcs = _XDCS(*args, **kwargs)
    return _xdcs
