#!/usr/bin/env sh

docker_file="Dockerfile"
previous_hash_file=".previous-hash"

if [ -f $previous_hash_file ]; then
    previous_hash=$(cat $previous_hash_file)
fi

if [ "Darwin" = $(uname) ]; then
    current_hash=$(md5 -q $docker_file)
else
    current_hash=$(md5sum $docker_file | awk '{ print $1 }')
fi

printf "${current_hash}" > ${previous_hash_file}

if [ "${previous_hash}" = "$current_hash" ]; then
    echo "false";
    exit 0;
else
    echo "true";
    exit 0;
fi;
