#!/usr/bin/env bash
set -e

DOCKER_COMPOSE_DIR="test-databases"

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
pushd ${DIR}/${DOCKER_COMPOSE_DIR}
docker-compose stop
docker-compose rm
docker-compose build
docker-compose up
popd