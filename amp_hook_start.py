#!/bin/env python3
# The start hook doesn't take any arguments (except possibly the --debug flag)
# but it is passed the AMP_ROOT and AMP_DATA_ROOT environment variables
# # This is a temporary tool to bootstrap the REST UI collection, until AMP-1893 is completed
import argparse
import os
from amp.config import load_amp_config
from amp.logging import setup_logging
import logging
import subprocess
import argparse
import logging
from urllib.request import Request, urlopen
from urllib.parse import quote
import time
import yaml
from pathlib import Path
import json

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--debug", default=False, action="store_true", help="Turn on debugging")
    args = parser.parse_args()

    setup_logging(None, args.debug)

    config = load_amp_config()

    # Check to see if we've already been successful
    if config['rest'].get('default_unit_created', False):
        logging.debug("Default unit has already been created")
        exit(0)

    url_base = f"http://{config['amp']['host']}:{config['amp']['port']}"
    amp_user = config['rest']['admin_username']
    amp_pass = config['rest']['admin_password']
    amp_root = Path(os.environ['AMP_ROOT'])

    # normally nobody knows these tunables exist, but there might be
    # some deployment scenarios where they would need changed.
    retries = config['rest'].get('default_unit_retries', 3)
    delay = config['rest'].get('default_unit_delay', 20)

    while retries >= 0:
        # get the token from the interface.
        try:
            req = Request(url_base + "/rest/account/authenticate", 
                        data=bytes(json.dumps({'username': amp_user, 'password': amp_pass}), encoding='utf-8'),
                        headers={'Content-Type': 'application/json'},
                        method="POST")
            with urlopen(req) as res:
                rdata = json.loads(res.read())
                amp_token = rdata['token']

            # see if the default unit is present
            default_unit = config['ui']['unit']
            req = Request(url_base + "/rest/units/search/findByName?name=" + quote(default_unit),
                        headers={'Authorization': f"Bearer {amp_token}"})
            with urlopen(req) as res:
                rdata = json.loads(res.read())
                
            if len(rdata['_embedded']['units']) == 0:
                # create the unit
                req = Request(url_base + "/rest/units",
                            headers={'Authorization': f"Bearer {amp_token}",
                                    'Content-Type': 'application/json'},
                            data=bytes(json.dumps({'name': default_unit}), encoding="utf-8"),
                            method="POST")
                with urlopen(req) as res:
                    if res.status == 201:
                        logging.info("Created default unit")
                        with open(amp_root / "data/package_config/amp_rest_default_unit.yaml", "w") as f:
                            yaml.safe_dump({'rest': {'default_unit_created': True}}, f)
                        break
                    
            else:
                # really, this should never happen since we shouldn't even be trying.
                logging.info("Unit already exists")
                break
        except Exception as e:
            logging.warning(f"Failed to set up default unit: {e}")                                
        
        retries -= 1
        time.sleep(delay)
    else:
        logging.error("Could not create default unit, after multiple retries")
        exit(1)

if __name__ == "__main__":
    main()
