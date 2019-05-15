#!/usr/bin/env bash
docker-compose down;
docker-compose build --build-arg IP_ADDRESS=$(ifconfig | sed -En 's/127.0.0.1//;s/.*inet (addr:)?(([0-9]*\.){3}[0-9]*).*/\2/p');
docker-compose up;