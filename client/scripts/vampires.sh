#!/usr/bin/env bash

randomdir=$(head /dev/urandom | tr -dc A-Za-z0-9 | head -c5)
export KAMON_SIGAR_FOLDER=/tmp/${randomdir}

# Attempt to set APP_HOME
# Resolve links: $0 may be a link
PRG="$0"
# Need this for relative symlinks.
while [ -h "$PRG" ] ; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '.*-> \(.*\)$'`
    if expr "$link" : '/.*' > /dev/null; then
        PRG="$link"
    else
        PRG=`dirname "$PRG"`"/$link"
    fi
done
SAVED="`pwd`"
cd "`dirname \"$PRG\"`/.." >/dev/null
APP_HOME="`pwd -P`"
cd "$SAVED" >/dev/null

${APP_HOME}/bin/client "$@"

# cleanup
rm -fr ${KAMON_SIGAR_FOLDER}