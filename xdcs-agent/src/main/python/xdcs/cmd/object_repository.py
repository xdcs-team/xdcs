import os
import shutil
import tempfile
from typing import IO, Generator, List
from zipfile import ZipFile

from xdcs.app import xdcs
from xdcs.cmd import Command
from xdcs_api.common_pb2 import Chunk
from xdcs_api.object_repository_pb2 import ObjectIds
from xdcs_api.object_repository_pb2_grpc import ObjectRepositoryStub


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
