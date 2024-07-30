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
source build-tools/tests.sh

assertMac "Mac is required for publishing"
assertGitRepo
assertCleanRepo

if [ -z "$BTFUSE_CODESIGN_IDENTITY" ]; then
    echo "BTFUSE_CODESIGN_IDENTITY environment variable is required."
    exit 2
fi

VERSION="$1"

assertVersion $VERSION
assetGitTagAvailable "ios/$VERSION"

echo $VERSION > ios/VERSION
BUILD_NO=$(< "./ios/BUILD")
BUILD_NO=$((BUILD_NO + 1))
echo $BUILD_NO > ./ios/BUILD

./buildIOS.sh
testIOS "Fuse iOS 17" "17.5" "iPhone 15" "BTFuseFilesystem" "BTFuseFilesystem"

git add ios/VERSION ios/BUILD
git commit -m "iOS Release: $VERSION"
git push
git tag -a ios/$VERSION -m "iOS Release: $VERSION"
git push --tags

gh release create ios/$VERSION \
    ./dist/ios/BTFuseFilesystem.xcframework.zip \
    ./dist/ios/BTFuseFilesystem.xcframework.zip.sha1.txt \
    ./dist/ios/BTFuseFilesystem.framework.dSYM.zip \
    ./dist/ios/BTFuseFilesystem.framework.dSYM.zip.sha1.txt \
    --verify-tag --generate-notes

pod spec lint BTFuseFilesystem.podspec
assertLastCall

pod repo push breautek BTFuseFilesystem.podspec
