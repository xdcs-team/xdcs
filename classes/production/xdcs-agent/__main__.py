import logging

from xdcs.app import xdcs

logging.basicConfig(level=logging.DEBUG)

if __name__ == '__main__':
    xdcs().run()
