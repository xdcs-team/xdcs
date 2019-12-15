import os
import tempfile
import unittest

from xdcs.cmd.object_repository import MaterializeTreeToObjectRepositoryCmd


class MaterializeTreeToObjectRepositoryCmdTest(unittest.TestCase):
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
