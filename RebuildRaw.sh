#!/bin/bash

cat src/main/resources/"$1"/raw.tar.xz__seg* > src/main/resources/"$1"/raw.tar.xz
mkdir src/main/resources/"$1"/raw
tar -xf src/main/resources/"$1"/raw.tar.xz -C src/main/resources/"$1"/raw
rm src/main/resources/"$1"/raw.tar.xz
