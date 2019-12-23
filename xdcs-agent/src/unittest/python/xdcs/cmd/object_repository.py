import json
import os
import tempfile
import unittest

from mockito import when

from xdcs.app import xdcs
from xdcs.cmd.object_repository import MaterializeTreeToObjectRepositoryCmd
from xdcs.object_repository import ObjectRepository


class MaterializeTreeToObjectRepositoryCmdTest(unittest.TestCase):
    def setUp(self) -> None:
        self.temporary_directory = tempfile.TemporaryDirectory()
        self.tmp = self.temporary_directory.__enter__()
        self.or_path = os.path.join(self.tmp, 'or')
        self._or = ObjectRepository(self.or_path)
        self.test_path = os.path.join(self.tmp, "test")
        when(xdcs()).object_repository().thenReturn(self._or)
        os.mkdir(self.test_path)

    def test_read_mode_file(self):
        with tempfile.TemporaryDirectory() as tmp:
            fpath = os.path.join(tmp, 'test')
            open(fpath, 'a').close()
            os.chmod(fpath, 0o644)

            self.assertEqual('100644', MaterializeTreeToObjectRepositoryCmd._read_mode(fpath))

    def test_read_mode_dir(self):
        with tempfile.TemporaryDirectory() as tmp:
            dpath = os.path.join(tmp, 'test')
            os.makedirs(dpath)
            os.chmod(dpath, 0o644)

            self.assertEqual('040644', MaterializeTreeToObjectRepositoryCmd._read_mode(dpath))

    def test_multiple_files(self):
        self.create_file(os.path.join(self.test_path, 'test1.txt'))
        self.create_file(os.path.join(self.test_path, 'test2.txt'))
        self.create_file(os.path.join(self.test_path, 'test3.txt'))
        root_id, obj = MaterializeTreeToObjectRepositoryCmd(self.test_path).execute()
        _json = json.loads(self._or.cat_bytes(root_id).decode("utf-8"))
        self.assertTrue(isinstance(_json, list))
        self.assertEqual(3, len(_json))
        self.assertEqual("test1.txt", _json[0]['name'])
        self.assertEqual("test2.txt", _json[1]['name'])
        self.assertEqual("test3.txt", _json[2]['name'])
        self.assertEqual("test", self._or.cat_bytes(_json[0]['id']).decode("utf-8"))
        self.assertEqual("test", self._or.cat_bytes(_json[1]['id']).decode("utf-8"))
        self.assertEqual("test", self._or.cat_bytes(_json[2]['id']).decode("utf-8"))

    def test_multiple_different_files(self):
        self.create_file(os.path.join(self.test_path, 'test1.txt'))
        os.mkdir(os.path.join(self.test_path, "dir"))
        link_path = os.path.join(self.test_path, 'link')
        os.symlink('wrong', link_path)
        root_id, obj = MaterializeTreeToObjectRepositoryCmd(self.test_path).execute()
        _json = self._or.cat_json(root_id)
        self.assertEqual("04", _json[0]['mode'][:2])
        self.assertEqual("12", _json[1]['mode'][:2])
        self.assertEqual("10", _json[2]['mode'][:2])

    def test_file_permission(self):
        file_path = os.path.join(self.test_path, "test1.txt")
        file = open(file_path, 'a')
        os.chmod(file_path, 0o444)
        file.write("test")
        file.close()
        root_id, obj = MaterializeTreeToObjectRepositoryCmd(self.test_path).execute()
        _json = self._or.cat_json(root_id)
        self.assertEqual("10", _json[0]['mode'][:2])
        self.assertEqual("0444", _json[0]['mode'][2:])

    def create_file(self, path):
        file = open(path, 'a')
        file.write("test")
        file.close()
