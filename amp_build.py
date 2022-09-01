#!/bin/env python3
#
# Build the amp rest tarball for distribution
#

import argparse
import logging
import tempfile
from pathlib import Path
import shutil
import sys
import yaml
from datetime import datetime
import os
import subprocess
import tarfile
import time
import io
import zipfile
import xml.etree.ElementTree as ET
from amp.package import *

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--debug', default=False, action='store_true', help="Turn on debugging")
    parser.add_argument('--package', default=False, action='store_true', help="build a package instead of installing")
    parser.add_argument('--clean', default=False, action='store_true', help="Clean previous build & dependencies")
    parser.add_argument('destdir', help="Output directory for package or webserver path root", nargs='?')
    args = parser.parse_args()
    logging.basicConfig(format="%(asctime)s [%(levelname)-8s] (%(filename)s:%(lineno)d)  %(message)s",
                        level=logging.DEBUG if args.debug else logging.INFO)

    if args.package and not args.destdir:
        logging.error("You must supply a destdir when building a package")
        exit(1)

    if args.destdir is not None:
        destdir = Path(args.destdir).resolve()
    else:
        destdir = None

    if 'JAVA_HOME' not in os.environ:
        logging.error("Please set the JAVA_HOME to a JDK11 Path (this won't build on JDK17)")
        exit(1)


    logging.info("Building REST WAR")
    os.chdir(sys.path[0])
    try:
        if args.clean:
            subprocess.run(['mvn', 'clean'], check=True)
        subprocess.run(['mvn', 'install', '-DskipTests'], check=True)
    except Exception as e:
        logging.error(f"Maven build failed: {e}")
        exit(1)


    warfile = list(Path("target").glob("*.war"))[0]

    if not args.package:
        logging.info(f"The war file is in: {warfile}")
        exit(0)

    # OK so the .war file is in the target directory.
    # find the version:
    root = ET.parse("pom.xml").getroot()
    version = root.find('{http://maven.apache.org/POM/4.0.0}version').text

    with tempfile.TemporaryDirectory() as tmpdir:
        shutil.copy(warfile, tmpdir + "/rest.war")
        pfile = create_package(Path(args.destdir), Path(tmpdir),
                            metadata={'name': 'amp_rest',
                                        'version': version,
                                        'install_path': 'tomcat/webapps'
                                        },
                            hooks={'post': 'amp_hook_post.py',
                                   'config': 'amp_hook_config.py',
                                   'start': 'amp_hook_start.py'},
                            defaults='amp_config.default',
                            depends_on=['tomcat', 'galaxy'])
                                
        logging.info(f"New package is in {pfile}")

        exit(0)


    buildtime = datetime.now().strftime("%Y%m%d_%H%M%S")
    basedir = f"amp_rest-{args.version}"
    pkgfile = Path(destdir, f"{basedir}.tar")
    with tarfile.open(pkgfile, "w") as tfile:
        # create base directory
        base_info = tarfile.TarInfo(name=basedir)
        base_info.mtime = int(time.time())
        base_info.type = tarfile.DIRTYPE
        base_info.mode = 0o755
        tfile.addfile(base_info, None)
        
        
        # write metadata file
        metafile = tarfile.TarInfo(name=f"{basedir}/amp_package.yaml")
        metafile_data = yaml.safe_dump({
            'name': 'amp_rest',
            'version': args.version,
            'build_date': buildtime,
            'install_path': 'tomcat/webapps'
        }).encode('utf-8')
        metafile.size = len(metafile_data)
        metafile.mtime = int(time.time())
        metafile.mode = 0o644
        tfile.addfile(metafile, io.BytesIO(metafile_data))

        # create the data directory
        data_info = tarfile.TarInfo(name=f'{basedir}/data')
        data_info.mtime = int(time.time())
        data_info.type = tarfile.DIRTYPE
        data_info.mode = 0o755
        tfile.addfile(data_info, None)

        logging.debug("Adding ROOT.war to tarball")
        tfile.add(warfile, f"{basedir}/data/rest.war")        
        logging.info(f"Build complete.  Package is in: {pkgfile}")
    

if __name__ == "__main__":
    main()
