import argparse
import logging

from xdcs.app import xdcs


def main():
    parser = argparse.ArgumentParser(description='XDCS agent software.')
    parser.add_argument('--config', nargs=1,
                        help='provide a custom config location')
    parser.add_argument('--debug', default=False, action='store_true',
                        help='produce debug output')
    args = parser.parse_args()

    if args.debug:
        logging.basicConfig(level=logging.DEBUG)
    else:
        logging.basicConfig(level=logging.INFO)

    config_location = None
    if args.config is not None and isinstance(args.config, list):
        config_location = args.config[0]
    xdcs(config_location=config_location).run()


if __name__ == '__main__':
    main()
