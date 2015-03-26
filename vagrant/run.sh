#!/bin/bash

#Defaults
#containers="postgres rabbitmq redis swagger"
containers="postgres rabbitmq redis"

if [[ "x$1" != "x" ]]; then
  containers=$1
fi

vagrant up --no-parallel --provider=docker $containers
