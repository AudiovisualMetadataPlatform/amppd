#!/bin/env python3
# This script will be run when the AMP system is reconfigured.  It will
# write the configuration files that amppd needs, driven by the amp
# configuration file.
#
# No arguments, but the AMP_ROOT and AMP_DATA_ROOT environment variables
# will be set by the caller so it can find all things AMP.

import argparse
import logging
from pathlib import Path
import os
import yaml
import subprocess
from amp.config import load_amp_config
from amp.logging import setup_logging

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--debug', default=False, action='store_true', help="Turn on debugging")    
    args = parser.parse_args()

    # set up the standard logging
    setup_logging(None, args.debug)

    # grab the configuration file
    config = load_amp_config()

    # set amp_root
    amp_root = Path(os.environ['AMP_ROOT'])


    """Create the configuration file for the AMP REST service"""
    # make sure the configuration file is specified in the tomcat startup env stuff:
    # JAVA_OPTS:  -Dspring.config.location=/path/to/config.properties
    if not (amp_root / "tomcat/bin/setenv.sh").exists():
        with open(amp_root / "tomcat/bin/setenv.sh", "w") as o:
            o.write(f'JAVA_OPTS="$JAVA_OPTS -Dspring.config.location={amp_root / "data/config/application.properties"!s}"\n')
    else:
        (amp_root / "tomcat/bin/setenv.sh").rename(amp_root / "tomcat/bin/setenv.sh.bak")        
        with open(amp_root / "tomcat/bin/setenv.sh.bak") as i:
            with open(amp_root / "tomcat/bin/setenv.sh", "w") as o:
                for l in i.readlines():
                    if 'spring.config.location' in l:
                        pass
                    elif l == '':
                        pass
                    else:
                        o.write(l)
                        o.write('\n')
                o.write(f'JAVA_OPTS="$JAVA_OPTS -Dspring.config.location={amp_root / "data/config/application.properties"!s}"\n')

    # create the configuration file, based on config data...
    with open(amp_root / "data/config/application.properties", "w") as f:
        # simple property map
        property_map = {
            # server port and root            
            'server.port': (['amp', 'port'], None),
            # database creds (host/db/port is handled elsewhere)
            'spring.datasource.username': (['rest', 'db_user'], None),
            'spring.datasource.password': (['rest', 'db_pass'], None),
            # initial user
            'amppd.username': (['rest', 'admin_username'], None),
            'amppd.password': (['rest', 'admin_password'], None), 
            'amppd.adminEmail': (['rest', 'admin_email'], None), 
            # galaxy integration
            "galaxy.host": (['galaxy', 'host'], 'localhost'),            
            "galaxy.root": (['galaxy', 'root'], None),            
            "galaxy.username": (['galaxy', 'admin_username'], None),
            "galaxy.password": (['galaxy', 'admin_password'], None),
            "galaxy.port": (['amp', 'galaxy_port'], None),  # set during galaxy config generation
            "galaxy.userId": (['galaxy', "user_id"], None), # set during galaxy config generation
            # AMPUI properties           
            'amppdui.hmgmSecretKey': (['mgms', 'hmgm', 'auth_key'], None),
            # Directories
            'amppd.fileStorageRoot': (['rest', 'storage_path'], 'media', 'path_rel', ['amp', 'data_root']),
            'amppd.dropboxRoot': (['rest', 'dropbox_path'], 'dropbox', 'path_rel', ['amp', 'data_root']),
            'logging.path': (['rest', 'logging_path'], 'logs', 'path_rel', ['amp', 'data_root']),
            'amppd.mediaprobeDir': (['rest', 'mediaprobe_dir'], 'MediaProbe', 'path_rel', ['amp', 'data_root']),
            # Avalon integration
            "avalon.url": (['rest', 'avalon_url'], 'https://avalon.example.edu'),
            "avalon.token": (['rest', 'avalon_token'], 'dummytoken'),
            # secrets             
            'amppd.encryptionSecret': (['rest', 'encryption_secret'], None), 
            'amppd.jwtSecret': (['rest', 'jwt_secret'], None),
        }
   
        def resolve_list(data, path, default=None):
            # given a data structure and a path, walk it and return the value
            if len(path) == 1:
                logging.debug(f"Base case: {data}, {path}, {default}")
                return data.get(path[0], default)
            else:
                v = data.get(path[0], None)
                logging.debug(f"Lookup: {data}, {path}, {default} = {v}")
                if v is None or not isinstance(v, dict):
                    logging.debug("Returning the default")
                    return default
                else:
                    logging.debug(f"Recurse: {v}, {path[1:]}, {default}")
                    return resolve_list(v, path[1:], default)

        # create the configuration
        for key, val in property_map.items():
            if isinstance(val, str):
                # this is a constant, just write it.
                f.write(f"{key} = {val}\n")
            elif isinstance(val, tuple):
                # every section starts with a reference list
                logging.debug(f"Looking up {key} {val}")
                v = resolve_list(config, val[0], val[1])
                if v is None:
                    logging.error(f"Error setting {key}:  Section {val[0]} doesn't exist in the configuration")
                    continue
                if len(val) < 3:
                    # write it.
                    if isinstance(v, bool):
                        f.write(f"{key} = {'true' if v else 'false'}\n")
                    else:
                        f.write(f"{key} = {v}\n")
                else:
                    # there's a function to be called.
                    if val[2] == 'path_rel':
                        if Path(v).is_absolute():
                            f.write(f"{key} = {v}\n")
                        else:
                            r = resolve_list(config, val[3], None)
                            if r is None:
                                logging.error(f"Error setting {key}:  Section {val[3]} doesn't exist in the configuration")
                                continue                        
                            this_path = None
                            if Path(r).is_absolute():
                                this_path = Path(r, v)
                            else:
                                this_path = Path(amp_root, r, v)
                            f.write(f"{key} = {this_path!s}\n")
                            # create the directory if we need to (need the check because it may be symlink)
                            if not this_path.exists():
                                this_path.mkdir(exist_ok=True)
                    else:
                        logging.error(f"Error handling {key}:  special action {val[2]} not supported")


        # these are things which are "hard" and can't be done through the generic mechanism.        
        # datasource configuration
        f.write(f"spring.datasource.url = jdbc:postgresql://{config['rest']['db_host']}:{config['rest'].get('db_port', 5432)}/{config['rest']['db_name']}\n")
        # amppdui.url and amppd.url -- where we can find the UI and ourselves.
        if config['amp'].get('use_https', False):
            f.write(f"amppdui.url = https://{config['amp']['host']}/#\n")
            f.write(f"amppd.url = https://{config['amp']['host']}/rest\n")
        else:
            f.write(f"amppdui.url = http://{config['amp']['host']}:{config['amp']['port']}/#\n")
            f.write(f"amppd.url = http://{config['amp']['host']}:{config['amp']['port']}/rest\n")
        #  amppdui.documentRoot -- this should be somewhere in the tomcat tree.
        f.write(f"amppdui.documentRoot = {amp_root}/tomcat/webapps/ROOT\n")
        f.write(f"amppdui.symlinkDir = {amp_root}/{config['amp']['data_root']}/symlinks\n")
                        
        f.write("# boilerplate properties\n")
        for k,v in config['rest']['properties'].items():
            if isinstance(v, bool):
                f.write(f"{k} = {'true' if v else 'false'}\n")
            else:
                f.write(f"{k} = {v}\n")



if __name__ == "__main__":
    main()