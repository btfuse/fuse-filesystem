
/*
Copyright Breautek 2024-2024

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

#import <Foundation/Foundation.h>
#import <BTFuseFilesystem/BTFuseFilesystemFileAPIFactory.h>
#import <BTFuseFilesystem/BTFuseFilesystemFileAPI.h>
#import <BTFuseFilesystem/BTFuseFilesystemFSAPIProto.h>

@implementation BTFuseFilesystemFileAPIFactory {
    id<BTFuseFilesystemFSAPIProto> $fsapi;
}

- (instancetype) init {
    self = [super init];
    
    $fsapi = [[BTFuseFilesystemFileAPI alloc] init];
    
    return self;
}

- (id<BTFuseFilesystemFSAPIProto>) get:(NSURL*) url {
    NSString* scheme = url.scheme;
    
    if (scheme == nil) {
        return nil;
    }
    
    if ([scheme isEqualToString: @"file"]) {
        return $fsapi;
    }

    return nil;
}

@end
