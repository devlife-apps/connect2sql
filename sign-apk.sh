#!/usr/bin/env bash

if [ $# -ne 3 ]; then
    echo "${0} <keystore> <alias_name> <apk>"
    exit 1
fi

keystore=$1
key_alias=$2
apk=$3

jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore "$keystore" "$apk" "$key_alias"

if [ $? -ne 0 ]; then
    echo "Could not sign apk!"
    exit 1
fi

jarsigner -verify -verbose -certs "$apk" | grep "jar verified"

if [ $? -ne 0 ]; then
    echo "Could not verify apk was signed!"
    exit 1
fi

build_tools_dir=`ls -1 $ANDROID_HOME/build-tools/ | tail -1`
build_tools_dir=$ANDROID_HOME/build-tools/$build_tools_dir

$build_tools_dir/zipalign -v 4 "$apk" "$apk-aligned.apk"

if [ $? -ne 0 ]; then
    echo "Could zipalign apk!"
    exit 1
fi