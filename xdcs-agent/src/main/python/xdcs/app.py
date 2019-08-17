_xdcs = None


def xdcs():
    global _xdcs
    if not _xdcs:
        from xdcs.xdcs import _XDCS
        _xdcs = _XDCS()
    return _xdcs
