import logging
import os
import shutil
import tempfile
from abc import abstractmethod
from os import path
from stat import ST_MODE
from typing import IO, Generator, List
from zipfile import ZipFile

from xdcs.app import xdcs
from xdcs.cmd import Command
from xdcs.object_repository import ObjectRepository
from xdcs_api.common_pb2 import Chunk
from xdcs_api.object_repository_pb2 import ObjectIds, DependencyResolutionRequest, ObjectKey, ObjectType
from xdcs_api.object_repository_pb2_grpc import ObjectRepositoryStub

logger = logging.getLogger(__name__)


class FetchObjectWithDependenciesCmd(Command):
    _object_id: str

    def __init__(self, object_id: str):
        self._object_id = object_id

    def execute(self):
        stub = ObjectRepositoryStub(xdcs().channel())
        req = DependencyResolutionRequest()
        object_key = ObjectKey()
        object_key.objectId = self._object_id
        object_key.objectType = self.get_type()
        req.objectKeys.extend([object_key])
        req.depth = 2 ** 32 - 1
        object_ids = stub.ResolveDependencies(req).objectIds
        object_ids.append(self._object_id)

        xdcs().execute(RetrieveObjectsCmd((ids for ids in [object_ids])))

    @abstractmethod
    def get_type(self) -> ObjectType:
        pass


class FetchTreeWithDependenciesCmd(FetchObjectWithDependenciesCmd):
    def __init__(self, tree_id: str):
        FetchObjectWithDependenciesCmd.__init__(self, tree_id)

    def get_type(self) -> ObjectType:
        return ObjectType.TREE


class FetchDeploymentWithDependenciesCmd(FetchObjectWithDependenciesCmd):
    def __init__(self, deployment_id: str):
        FetchObjectWithDependenciesCmd.__init__(self, deployment_id)

    def get_type(self) -> ObjectType:
        return ObjectType.DEPLOYMENT


class RetrieveObjectsCmd(Command):
    _object_ids: Generator[List[str], None, None]

    def __init__(self, object_ids: Generator[List[str], None, None]):
        self._object_ids = object_ids

    def execute(self):
        stub = ObjectRepositoryStub(xdcs().channel())
        chunks = stub.RequestObjects(self.__generate_requests())

        tmp_dir = tempfile.mkdtemp()
        try:
            zip_path = tmp_dir + '/zip'
            # download the zip file
            with open(zip_path, 'wb') as zipfile:
                self.__write_chunks(zipfile, chunks)

            # extract objects
            with ZipFile(zip_path) as zipfile:
                zipfile.extractall(tmp_dir)
                objects = zipfile.namelist()

            # remove the zip file
            os.remove(zip_path)

            obj_repo = xdcs().object_repository()
            for obj in objects:
                obj_repo.import_object(tmp_dir + '/' + obj, required_id=obj)
        finally:
            shutil.rmtree(tmp_dir)

    def __write_chunks(self, zipfile: IO, chunks):
        for chunk in chunks:
            chunk: Chunk
            zipfile.write(chunk.content)

    def __generate_requests(self):
        for ids in self._object_ids:
            requested_objects = ObjectIds()
            requested_objects.objectIds.extend(ids)
            yield requested_objects


class DumpObjectRepositoryTreeCmd(Command):
    _root_id: str
    _out_path: str

    def __init__(self, root_id: str, out_path: str):
        self._root_id = root_id
        self._out_path = out_path

    def execute(self):
        self._dump_tree(self._root_id, self._out_path)

    def _dump_tree(self, root_id, out_path):
        entries = xdcs().object_repository().cat_json(root_id)
        for entry in entries:
            file_path = path.join(out_path, entry['name'])
            mode = entry['mode']
            object_id = entry['id']

            object_type = mode[:2]
            permissions = int(mode[2:], 8)

            if object_type == '12':
                content = xdcs().object_repository().cat_bytes(object_id)
                os.symlink(content, file_path)
                os.chmod(file_path, permissions)
            elif object_type == '10':
                content = xdcs().object_repository().cat_bytes(object_id)
                with open(file_path, 'wb+') as fh:
                    fh.write(content)
                os.chmod(file_path, permissions)
            elif object_type == '04':
                os.mkdir(file_path, permissions)
                self._dump_tree(object_id, file_path)


class UploadObjectsCmd(Command):
    _CHUNK_SIZE = 8 * 1024

    _object_ids: List[str]
    _or: ObjectRepository

    def __init__(self, object_ids: List[str]):
        self._object_ids = object_ids
        self._or = xdcs().object_repository()

    def execute(self):
        logger.debug("Uploading objects: " + str(self._object_ids))
        stub = ObjectRepositoryStub(xdcs().channel())

        tmp_dir = tempfile.mkdtemp()
        try:
            objects_path = tmp_dir + '/objects'
            os.makedirs(objects_path)

            for obj_id in self._object_ids:
                self._or.cp(obj_id, os.path.join(objects_path, obj_id))

            zip_path = shutil.make_archive(tmp_dir + '/zip', 'zip', objects_path)

            stub.UploadObjects(self.__read_chunks(zip_path))
            logger.debug("Objects uploaded")
        finally:
            shutil.rmtree(tmp_dir)

    def __read_chunks(self, file_path: str):
        with open(file_path, 'rb') as f:
            while True:
                r = f.read(self._CHUNK_SIZE)
                if r is None or len(r) == 0:
                    return
                yield Chunk(content=r)


class MaterializeTreeToObjectRepositoryCmd(Command):
    _in_path: str
    _or: ObjectRepository

    def __init__(self, in_path: str):
        self._in_path = in_path
        self._or = xdcs().object_repository()

    def execute(self):
        all_objects = []
        root_id = self._materialize_tree(self._in_path, all_objects)
        return root_id, all_objects

    def _materialize_tree(self, fpath, all_objects: list) -> str:
        (_, dirnames, files) = next(os.walk(fpath))
        entries = []

        for file in files:
            entry = {
                'id': self._or.import_object(os.path.join(fpath, file)),
                'name': file,
                'mode': self._read_mode(os.path.join(fpath, file))
            }
            entries.append(entry)
            all_objects.append(entry['id'])

        for dirname in dirnames:
            entry = {
                'id': self._materialize_tree(os.path.join(fpath, dirname), all_objects),
                'name': dirname,
                'mode': self._read_mode(os.path.join(fpath, dirname))
            }
            entries.append(entry)

        entries.sort(key=lambda e: e['name'])
        ret_id = self._or.import_json(entries)
        all_objects.append(ret_id)
        return ret_id

    @staticmethod
    def _read_mode(fpath: str) -> str:
        return '%06o' % os.stat(fpath, follow_symlinks=False)[ST_MODE]
