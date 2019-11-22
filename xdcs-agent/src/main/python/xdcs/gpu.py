from typing import Dict, Optional


class GpuDevice:
    def nvidia_id(self) -> str:
        return ''

    def is_nvidia(self) -> bool:
        return False


class _GpuManager:
    _devices_by_key: Dict[str, GpuDevice]

    def __init__(self) -> None:
        pass

    def device_by_key(self, key: str) -> Optional[GpuDevice]:
        return self._devices_by_key[key]


manager = _GpuManager()
