import hashlib
import json
import os
import re
import shutil
from os import path


class ObjectRepositoryException(Exception):
    pass


class ObjectRepository:
    _object_id_pattern = re.compile("^[0-9a-z]{40}$")

    _path: str

    def __init__(self, path: str) -> None:
        self._path = path

    def import_object(self, path: str, required_id: str = None) -> str:
        object_id = self._hash(path)

        if required_id is not None and required_id != object_id:
            raise ObjectRepositoryException('Import failed: wrong checksum for {}'.format(path))

        object_path = self.__to_path(object_id, True)

        if os.path.islink(path):
            with open(object_path, "w") as f:
                f.write(os.readlink(path))
        else:
            shutil.copyfile(path, object_path)

        return object_id

    def import_json(self, obj: object) -> str:
        json_str = json.dumps(obj, separators=(',', ':'))
        sha1 = hashlib.sha1()
        sha1.update(json_str.encode('utf-8'))
        object_id = sha1.hexdigest()

        object_path = self.__to_path(object_id, True)

        with open(object_path, "w") as f:
            f.write(json_str)

        return object_id

    def _hash(self, path: str) -> str:
        sha1 = hashlib.sha1()

        if os.path.islink(path):
            sha1.update(os.readlink(path).encode("utf-8"))
        else:
            with open(path, 'rb') as f:
                while True:
                    data = f.read(4 * 1024)
                    if not data:
                        break
                    sha1.update(data)

        return sha1.hexdigest()

    def __to_path(self, object_id: str, create_dirs=False) -> str:
        if not self._object_id_pattern.match(object_id):
            raise ObjectRepositoryException('Invalid object ID: ' + object_id)

        containing_dir = path.join(self._path, object_id[:2])

        if create_dirs:
            os.makedirs(containing_dir, exist_ok=True)

        return path.join(containing_dir, object_id[2:])

    def cat_bytes(self, object_id: str) -> bytes:
        with open(self._path + '/' + object_id[:2] + '/' + object_id[2:], "rb") as f:
            return f.read()

    def cat_json(self, object_id: str) -> dict:
        return json.loads(self.cat_bytes(object_id))

    def cp(self, object_id: str, out_path: str) -> None:
        object_path = self.__to_path(object_id, True)
        shutil.copyfile(object_path, out_path)


def from_path(path: str) -> ObjectRepository:
    return ObjectRepository(path)
