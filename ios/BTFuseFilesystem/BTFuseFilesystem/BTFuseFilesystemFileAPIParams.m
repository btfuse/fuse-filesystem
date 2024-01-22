
/*
Copyright Breautek

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
#import "BTFuseFilesystemFileAPIParams.h"

const int BTFuseFilesystemFileAPIParams_CONTENT_LENGTH_BYTE_SIZE = 4;

@implementation BTFuseFilesystemFileAPIParams {
    NSData* $params;
    NSNumber* $contentLength;
}

- (instancetype) init {
    return [super init];
}

+ (BTFuseFilesystemFileAPIParams*) parse:(NSNumber*) contentLengthHv input:(NSInputStream*) io error:(BTFuseError**) error {
    BTFuseFilesystemFileAPIParams* params = [[BTFuseFilesystemFileAPIParams alloc] init];
    
    uint8_t contentLength[BTFuseFilesystemFileAPIParams_CONTENT_LENGTH_BYTE_SIZE];
    NSInteger bytesRead = [io read:contentLength maxLength:BTFuseFilesystemFileAPIParams_CONTENT_LENGTH_BYTE_SIZE];
    
    if (bytesRead < BTFuseFilesystemFileAPIParams_CONTENT_LENGTH_BYTE_SIZE) {
        *error = [[BTFuseError alloc] init:@"FuseFilesystem" withCode:0 withMessage:@"Unable to read Fuse File API Params length byte."];
        return nil;
    }
    
    int contentLengthInt = ((contentLength[0] & 0xFF) << 24) |
                ((contentLength[1] & 0xFF) << 16) |
                ((contentLength[2] & 0xFF) << 8) |
                (contentLength[3] & 0xFF);
                
    uint8_t content[contentLengthInt];
    
    bytesRead = [io read:content maxLength:contentLengthInt];
    if (bytesRead < contentLengthInt) {
        *error = [[BTFuseError alloc] init:@"FuseFilesystem" withCode:0 withMessage:@"Unable to read Fuse File API Params content."];
        return nil;
    }
    
    params->$contentLength = contentLengthHv;
    params->$params = [[NSData alloc] initWithBytes: content length: contentLengthInt];
    
    return params;
}

- (NSNumber*) getContentLength {
    return [[NSNumber alloc] initWithUnsignedLong: [$contentLength unsignedLongValue] - [$params length] - BTFuseFilesystemFileAPIParams_CONTENT_LENGTH_BYTE_SIZE];
}

- (NSData*) getParams {
    return $params;
}

@end
