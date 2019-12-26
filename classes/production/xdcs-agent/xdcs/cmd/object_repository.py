import os
import shutil
import tempfile
from os import path
from typing import IO, Generator, List
from zipfile import ZipFile

from xdcs.app import xdcs
from xdcs.cmd import Command
from xdcs_api.common_pb2 import Chunk
from xdcs_api.object_repository_pb2 import ObjectIds, DependencyResolutionRequest, ObjectKey, ObjectType
from xdcs_api.object_repository_pb2_grpc import ObjectRepositoryStub


class FetchDeploymentCmd(Command):
    _deployment_id: str

    def __init__(self, deployment_id: str):
        self._deployment_id = deployment_id

    def execute(self):
        stub = ObjectRepositoryStub(xdcs().channel())
        req = DependencyResolutionRequest()
        deployment_key = ObjectKey()
        deployment_key.objectId = self._deployment_id
        deployment_key.objectType = ObjectType.DEPLOYMENT
        req.objectKeys.extend([deployment_key])
        req.depth = 2 ** 32 - 1
        object_ids = stub.ResolveDependencies(req).objectIds
        object_ids.append(self._deployment_id)

        xdcs().execute(RetrieveObjectsCmd((ids for ids in [object_ids])))


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
