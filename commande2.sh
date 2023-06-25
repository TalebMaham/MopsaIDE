#!/bin/bash

if [ -f "configure.sh" ] && [ -f "Makefile" ]; then

    ./configure CC=clang


    mopsa-build make
else
    mopsa-python *.py -format=json >analyse.json
fi
