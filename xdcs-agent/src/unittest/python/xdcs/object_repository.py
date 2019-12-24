import os
import tempfile
import unittest

from xdcs.object_repository import ObjectRepository, ObjectRepositoryException


class ObjectRepositoryTest(unittest.TestCase):
    _or = None
    fpath = None
    tmp = None
    temporary_directory = None
    or_path = None
    expected_hash = "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3"

    def setUp(self) -> None:
        self.temporary_directory = tempfile.TemporaryDirectory()
        self.tmp = self.temporary_directory.__enter__()
        self.or_path = os.path.join(self.tmp, 'or')
        self._or = ObjectRepository(self.or_path)
        self.fpath = os.path.join(self.tmp, 'test.txt')
        file = open(self.fpath, 'a')
        file.write("test")
        file.close()

    def tearDown(self) -> None:
        self.temporary_directory.cleanup()

    def test_hash_empty_file(self):
        self.assertEqual(self.expected_hash, self._or._hash(self.fpath))

    def test_import_object_wrong_objectid(self):
        wrong_hash = "wrong_hash"
        self.assertRaises(ObjectRepositoryException, self._or.import_object, self.fpath, wrong_hash)

    def test_import_object_correct_path(self):
        object_id = self._or.import_object(self.fpath)
        self.assert_object_created(object_id)

    def assert_object_created(self, object_id):
        self.assertTrue(os.path.exists(self.or_path))
        self.assertTrue(os.path.exists(os.path.join(self.or_path, object_id[:2])))
        self.assertTrue(os.path.exists(os.path.join(self.or_path, object_id[:2], object_id[2:])))

    def test_directories_names(self):
        self._or.import_object(self.fpath)
        self.assert_object_created(self.expected_hash)

    def test_cat(self):
        object_id = self._or.import_object(self.fpath)
        self.assertEqual("test", self._or.cat_bytes(object_id).decode("utf-8"))

    def test_hash_on_symlink(self):
        link_path = os.path.join(self.tmp, 'link')
        os.symlink('test', link_path)
        self.assertEqual(self.expected_hash, self._or._hash(link_path))

    def test_import_object_on_symlink(self):
        link_path = os.path.join(self.tmp, 'link')
        os.symlink(self.fpath, link_path)
        object_id = self._or.import_object(link_path)
        self.assert_object_created(object_id)

