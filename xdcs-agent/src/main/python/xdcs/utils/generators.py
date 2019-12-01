from queue import Queue
from typing import Generator, Any


def queue_generator(q: Queue, terminator=None) -> Generator[Any, None, None]:
    while True:
        x = q.get(True)
        if x == terminator:
            break
        yield x
