#!/bin/bash

cat src/main/resources/"$1"/oracle.tar.xz__seg* > src/main/resources/"$1"/oracle.tar.xz
mkdir src/main/resources/"$1"/oracle
tar -xf src/main/resources/"$1"/oracle.tar.xz -C src/main/resources/"$1"/oracle
rm src/main/resources/"$1"/oracle.tar.xz
