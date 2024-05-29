#!/bin/bash

# Copyright 2023 Breautek 

# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at

#     http://www.apache.org/licenses/LICENSE-2.0

# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

source build-tools/assertions.sh
source build-tools/DirectoryTools.sh
source build-tools/Checksum.sh

assertMac "Mac is required to build Fuse Filesystem Android"

echo "Building Fuse Filesystem Android Framework $(cat ./android/VERSION)..."

rm -rf ./dist/android
mkdir -p ./dist/android

echo "Cleaning the workspace..."
spushd android
    gradle wrapper
    assertLastCall
    ./gradlew clean
    assertLastCall
    ./gradlew :filesystem:build
    assertLastCall

    cp filesystem/build/outputs/aar/*.aar ../dist/android/
    assertLastCall
spopd

spushd dist/android
    sha1_compute filesystem-debug.aar
    sha1_compute filesystem-release.aar
spopd
