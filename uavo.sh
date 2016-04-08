#!/usr/bin/env bash

git clone https://bitbucket.org/librepilot/librepilot.git
cd librepilot
git checkout next
make uavobjects
./build/uavobjgenerator/uavobjgenerator -java shared/uavobjectdefinition .
ls -al
cd ..