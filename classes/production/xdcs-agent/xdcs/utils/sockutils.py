import select


def couple_socks(sock1, sock2, buffer_size=(4 * 1024)):
    while True:
        r, w, x = select.select([sock1, sock2], [], [])
        if sock1 in r:
            data = sock1.recv(buffer_size)
            if len(data) == 0:
                break
            sock2.send(data)
        if sock2 in r:
            data = sock2.recv(buffer_size)
            if len(data) == 0:
                break
            sock1.send(data)

    sock1.close()
    sock2.close()
