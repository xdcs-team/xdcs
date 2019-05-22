from mockito import mock, verify
import unittest

from xdcs import app

class AppTest(unittest.TestCase):
    def test_should_issue_hello_world_message(self):
        out = mock()

        app.run(out)

        verify(out).write("Hello world of Python")
