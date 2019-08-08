token = None
channel = None


def set_token(new_token):
    global token
    token = new_token


def get_token():
    return token


def set_channel(new_channel):
    global channel
    channel = new_channel
    return None


def get_channel():
    return channel
