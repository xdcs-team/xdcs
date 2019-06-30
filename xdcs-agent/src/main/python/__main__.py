import logging
import sys

from xdcs import app

logging.basicConfig(level=logging.INFO)

if __name__ == '__main__':
    app.run(sys.stdout)
