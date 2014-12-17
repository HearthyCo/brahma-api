#!/bin/bash

echo "*** Creating test database..."

gosu postgres postgres --single -E <<-EOSQL
    CREATE DATABASE "brahmatest"
EOSQL