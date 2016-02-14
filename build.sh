#!/usr/bin/env bash

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
cd "`dirname \"$PRG\"`" >/dev/null

git fetch --all
git reset --hard origin/master
./gradlew clean check installLocal


AWS_CREDENTIALS=${HOME}/.aws
if [[ -f  ${AWS_CREDENTIALS} ]]; then
    source ${AWS_CREDENTIALS}
    s3cmd put --access_key=${accessKey} --secret_key=${secretKey} -P client/build/distributions/*.zip s3://uva-vampires/client/
fi

cd "$SAVED" >/dev/null
