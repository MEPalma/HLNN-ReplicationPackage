#!/bin/bash

cat src/main/resources/"$1"/folds.tar.xz__seg* > src/main/resources/"$1"/folds.tar.xz
mkdir src/main/resources/"$1"/folds
tar -xf src/main/resources/"$1"/folds.tar.xz -C src/main/resources/"$1"/
rm src/main/resources/"$1"/folds.tar.xz
