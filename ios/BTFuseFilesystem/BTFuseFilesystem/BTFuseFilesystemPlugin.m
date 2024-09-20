
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
#import <BTFuse/BTFuse.h>
#import <BTFuseFilesystem/BTFuseFilesystemPlugin.h>
#import "BTFuseFilesystemFileTypeHandler.h"
#import <BTFuseFilesystem/BTFuseFilesystemFileType.h>
#import <BTFuseFilesystem/BTFuseFilesystemFileAPIFactory.h>
#import "BTFuseFilesystemFileAPIParams.h"
#import "BTFuseFilesystem/BTFuseFilesystemVars.h"

@implementation BTFuseFilesystemReadCallbackContext {
    BTFuseAPIResponse* $response;
}

- (instancetype) init:(BTFuseAPIResponse*) response {
    self = [super init];
    
    $response = response;
    
    return self;
}

- (void) onReadStart:(long) contentLength {
    [$response setStatus: contentLength > 0 ? 200 : 204];
    [$response setContentType:@"application/octet-stream"];
    [$response setContentLength: contentLength];
    [$response didFinishHeaders];
}

- (void) onReadChunk:(NSData*) chunk {
    [$response pushData: chunk];
}

- (void) onReadClose {
    [$response didFinish];
}

- (void) onReadFailure:(BTFuseError*) error {
    [$response kill: [error getMessage]];
}

- (void) dealloc {
    $response = nil;
}

@end

@implementation BTFuseFilesystemPlugin {
    uint32_t DEFAULT_CHUNK_SIZE;
    uint32_t $chunkSize;
    BTFuseFilesystemFileAPIFactory* $apiFactory;
}

- (instancetype) init:(BTFuseContext*) context {
    self = [super init: context];
    
    DEFAULT_CHUNK_SIZE = 4194304; // 4mb
    $chunkSize = DEFAULT_CHUNK_SIZE;
    
    $apiFactory = [[BTFuseFilesystemFileAPIFactory alloc] init];
    
    return self;
}

- (NSString*) getID {
    return BTFUSE_FILESYSTEM_TAG;
}

- (void) setChunkSize:(uint32_t) chunkSize {
    $chunkSize = chunkSize;
}

- (uint32_t) getChunkSize {
    return $chunkSize;
}

- (void) setAPIFactory:(BTFuseFilesystemFileAPIFactory*) factory {
    $apiFactory = factory;
}

- (BTFuseFilesystemFileAPIFactory*) getAPIFactory {
    return $apiFactory;
}

- (void) initHandles {
    [self attachHandler:@"/file/type" callback: ^(BTFuseAPIPacket* packet, BTFuseAPIResponse* response) {
        [self handleFileTypeRequest: packet response: response];
    }];
    
    [self attachHandler:@"/file/size" callback: ^(BTFuseAPIPacket* packet, BTFuseAPIResponse* response) {
        [self handleFileSizeRequest: packet response: response];
    }];
    
    [self attachHandler:@"/file/mkdir" callback: ^(BTFuseAPIPacket* packet, BTFuseAPIResponse* response) {
        [self handleFileMkdirRequest: packet response: response];
    }];
    
    [self attachHandler:@"/file/read" callback: ^(BTFuseAPIPacket* packet, BTFuseAPIResponse* response) {
        [self handleFileReadRequest: packet response: response];
    }];
    
    [self attachHandler:@"/file/truncate" callback: ^(BTFuseAPIPacket* packet, BTFuseAPIResponse* response) {
        [self handleFileTruncateRequest: packet response: response];
    }];
    
    [self attachHandler:@"/file/append" callback: ^(BTFuseAPIPacket* packet, BTFuseAPIResponse* response) {
        [self handleFileAppendRequest: packet response: response];
    }];
    
    [self attachHandler:@"/file/write" callback: ^(BTFuseAPIPacket* packet, BTFuseAPIResponse* response) {
        [self handleFileWriteRequest: packet response: response];
    }];
    
    [self attachHandler:@"/file/remove" callback: ^(BTFuseAPIPacket* packet, BTFuseAPIResponse* response) {
        [self handleFileRemoveRequest: packet response: response];
    }];
    
    [self attachHandler:@"/file/exists" callback: ^(BTFuseAPIPacket* packet, BTFuseAPIResponse* response) {
        [self handleFileExistsRequest: packet response: response];
    }];
}

- (void) handleFileTypeRequest:(BTFuseAPIPacket*) packet response:(BTFuseAPIResponse*) response {
    NSURL* url = [[NSURL alloc] initWithString: [packet readAsString]];
    BTFuseFilesystemFileAPIFactory* factory = [self getAPIFactory];
    id<BTFuseFilesystemFSAPIProto> fsapi = [factory get: url];
    
    BTFuseError* error = nil;
    BTFuseFilesystemFileType type = [fsapi getType: url error: &error];
    
    if (error != nil) {
        [response sendError: error];
        return;
    }
    
//    NSFileManager* fm = [NSFileManager defaultManager];
//    
//    BOOL isDirectory;
//    BOOL exists = [fm fileExistsAtPath: path isDirectory: &isDirectory];
//    
//    if (!exists) {
//        NSString* message = [NSString stringWithFormat:@"No such file found at %@", path];
//        [response sendError:[[BTFuseError alloc] init:@"FuseFilesystem" withCode:0 withMessage:message]];
//        return;
//    }
//    
//    BTFuseFilesystemFileType type = BTFuseFilesystemFileTypeFile;
//    
//    if (isDirectory) {
//        type = BTFuseFilesystemFileTypeDirectory;
//    }
//    
//    NSInteger typeInt = (NSInteger) type;
    
    [response sendString:[NSString stringWithFormat:@"%ld", (long) type]];
}

- (void) handleFileSizeRequest:(BTFuseAPIPacket*) packet response:(BTFuseAPIResponse*) response {
    NSURL* url = [[NSURL alloc] initWithString: [packet readAsString]];
    BTFuseFilesystemFileAPIFactory* factory = [self getAPIFactory];
    id<BTFuseFilesystemFSAPIProto> fsapi = [factory get: url];
    
//    BTFuseError* error = nil;
//    NSString* path = [packet readAsString];
    
    BTFuseError* error = nil;
//    NSNumber* size = [self $getFileSize: path error:&error];

    long size = [fsapi getSize: url error: &error];
    
    if (error != nil) {
        [response sendError:error];
        return;
    }

    [response sendString:[NSString stringWithFormat:@"%lu", size]];
}

- (void) handleFileMkdirRequest:(BTFuseAPIPacket*) packet response:(BTFuseAPIResponse*) response {
    NSError* error = nil;
    NSDictionary* params = [packet readAsJSONObject: error];
    
    if (error != nil) {
        [response sendError:[[BTFuseError alloc] init:@"FuseFilesystem" withCode:0 withError:error]];
        return;
    }
    
    NSURL* url = [[NSURL alloc] initWithString: [params objectForKey:@"path"]];
    BTFuseFilesystemFileAPIFactory* factory = [self getAPIFactory];
    id<BTFuseFilesystemFSAPIProto> fsapi = [factory get: url];
    
    bool recursive = [params objectForKey:@"recursive"];

    BTFuseError* fuseError = nil;
    bool didCreate = [fsapi mkdir: url recursive: recursive error: &fuseError];
    if (error != nil) {
        [response sendError: fuseError];
        return;
    }

    NSString* output = didCreate ? @"true" : @"false";
    [response sendString:output];
}

- (void) handleFileReadRequest:(BTFuseAPIPacket*) packet response:(BTFuseAPIResponse*) response {
    NSError* error = nil;
    NSDictionary* params = [packet readAsJSONObject: error];
    
    if (error != nil) {
        [response sendError:[[BTFuseError alloc] init: BTFUSE_FILESYSTEM_TAG withCode:0 withError:error]];
        return;
    }
    
    NSURL* url = [[NSURL alloc] initWithString: [params objectForKey:@"path"]];
    NSNumber* ndesiredLength = [params objectForKey:@"length"];
    NSNumber* noffset = [params objectForKey:@"offset"];
    
    BTFuseFilesystemFileAPIFactory* factory = [self getAPIFactory];
    id<BTFuseFilesystemFSAPIProto> fsapi = [factory get: url];
    
    BTFuseFilesystemReadCallbackContext* callbackContext = [[BTFuseFilesystemReadCallbackContext alloc] init: response];
    
    BTFuseError* fuseError = nil;
    [
        fsapi
        read: url
        length: [ndesiredLength longValue]
        offset: [noffset longValue]
        chunkSize: [self getChunkSize]
        callback:callbackContext
        error:&fuseError
    ];
    
    if (fuseError != nil) {
        [response sendError: fuseError];
        return;
    }
}

- (void) handleFileTruncateRequest:(BTFuseAPIPacket*) packet response:(BTFuseAPIResponse*) response {
    NSNumber* overallContentLength = [[NSNumber alloc] initWithUnsignedLong: [packet getContentLength]];
    NSInputStream* readStream = [[packet getClient] getInputStream];
    BTFuseStreamReader* reader = [[BTFuseStreamReader alloc] init: readStream];
    BTFuseError* error = nil;
    
    BTFuseFilesystemFileAPIParams* params = [BTFuseFilesystemFileAPIParamsParser parse: overallContentLength chunkSize: [self getChunkSize] reader: reader error: &error];
    
    if (error != nil) {
        [response sendError: error];
        return;
    }
    
    NSString* path = [[NSString alloc] initWithData: [params getParams] encoding: NSUTF8StringEncoding];
    NSURL* url = [[NSURL alloc] initWithString: path];
    BTFuseFilesystemFileAPIFactory* factory = [self getAPIFactory];
    id<BTFuseFilesystemFSAPIProto> fsapi = [factory get: url];
    
    NSUInteger contentLength = [[params getContentLength] unsignedLongValue];
    
    long bytesWritten = [
        fsapi
        truncate: url
        contentLength: contentLength
        input: reader
        chunkSize: [self getChunkSize]
        error: &error
    ];
    
    if (error != nil) {
        [response sendError: error];
        return;
    }
    
    [response sendString: [NSString stringWithFormat: @"%ld", bytesWritten]];
}

- (void) handleFileAppendRequest:(BTFuseAPIPacket*) packet response:(BTFuseAPIResponse*) response {
    NSNumber* overallContentLength = [[NSNumber alloc] initWithUnsignedLong: [packet getContentLength]];
    NSInputStream* readStream = [[packet getClient] getInputStream];
    BTFuseStreamReader* reader = [[BTFuseStreamReader alloc] init: readStream];
    BTFuseError* error = nil;
    
    BTFuseFilesystemFileAPIParams* params = [BTFuseFilesystemFileAPIParamsParser parse: overallContentLength chunkSize: [self getChunkSize] reader: reader error: &error];
    
    if (error != nil) {
        [response sendError: error];
        return;
    }
    
    NSURL* url = [[NSURL alloc] initWithString: [[NSString alloc] initWithData: [params getParams] encoding: NSUTF8StringEncoding]];
    
    BTFuseFilesystemFileAPIFactory* factory = [self getAPIFactory];
    id<BTFuseFilesystemFSAPIProto> fsapi = [factory get: url];
    
    long bytesWritten = [
        fsapi
        append: url
        input: reader
        contentLength: [[params getContentLength] unsignedLongValue]
        chunkSize: [self getChunkSize]
        error: &error
    ];
    
    if (error != nil) {
        [response sendError: error];
        return;
    }
    
    [response sendString: [NSString stringWithFormat: @"%ld", bytesWritten]];
}

- (void) handleFileWriteRequest:(BTFuseAPIPacket*) packet response:(BTFuseAPIResponse*) response {
    NSNumber* overallContentLength = [[NSNumber alloc] initWithUnsignedLong: [packet getContentLength]];
    NSInputStream* readStream = [[packet getClient] getInputStream];
    BTFuseStreamReader* reader = [[BTFuseStreamReader alloc] init: readStream];
    BTFuseError* error = nil;
    
    BTFuseFilesystemFileAPIParams* params = [BTFuseFilesystemFileAPIParamsParser parse: overallContentLength chunkSize: [self getChunkSize] reader: reader error: &error];
    
    if (error != nil) {
        [response sendError: error];
        return;
    }
    
    NSUInteger contentLength = [[params getContentLength] unsignedLongValue];
    if (contentLength == 0) {
        [response sendString:@"0"];
        return;
    }
    
    NSError* jsonError = nil;
    NSDictionary* opts = [NSJSONSerialization JSONObjectWithData: [params getParams] options: 0 error: &jsonError];
    if (jsonError != nil) {
        [response sendError:[[BTFuseError alloc] init: BTFUSE_FILESYSTEM_TAG withCode: 0 withError: jsonError]];
        return;
    }
    
    NSString* path = [opts objectForKey: @"path"];
    if (path == nil) {
        [response sendError:[[BTFuseError alloc] init:BTFUSE_FILESYSTEM_TAG withCode: 0 withMessage: @"Path is required"]];
        return;
    }
    
    NSNumber* nsoffset = [opts objectForKey:@"offset"];
    NSUInteger offset = 0;
    if (nsoffset != nil) {
        offset = [nsoffset unsignedLongValue];
    }
    
    NSURL* url = [[NSURL alloc] initWithString: path];
    BTFuseFilesystemFileAPIFactory* factory = [self getAPIFactory];
    id<BTFuseFilesystemFSAPIProto> fsapi = [factory get: url];
    
    long bytesWritten = [fsapi write: url offset: offset chunkSize: [self getChunkSize] input: reader contentLength: contentLength error: &error];
    if (error != nil) {
        [response sendError: error];
        return;
    }
    
    [response sendString: [NSString stringWithFormat: @"%ld", bytesWritten]];
}

- (void) handleFileRemoveRequest:(BTFuseAPIPacket*) packet response:(BTFuseAPIResponse*) response {
    NSError* error = nil;
    
    NSDictionary* opts = [packet readAsJSONObject: error];
    
    if (error != nil) {
        [response sendError:[[BTFuseError alloc] init:BTFUSE_FILESYSTEM_TAG withCode: 0 withError: error]];
        return;
    }
    
    NSURL* url = [[NSURL alloc] initWithString: [opts objectForKey: @"path"]];
    BTFuseFilesystemFileAPIFactory* factory = [self getAPIFactory];
    id<BTFuseFilesystemFSAPIProto> fsapi = [factory get: url];

    BTFuseError* fuseError = nil;
    bool didDelete = [fsapi delete: url recursive: [opts objectForKey: @"recursive"] error: &fuseError];
    if (fuseError != nil) {
        [response sendError: fuseError];
        return;
    }
    
    [response sendString: didDelete ? @"true" : @"false"];
}

- (void) handleFileExistsRequest:(BTFuseAPIPacket*) packet response:(BTFuseAPIResponse*) response {
    NSURL* url = [[NSURL alloc] initWithString: [packet readAsString]];
    BTFuseFilesystemFileAPIFactory* factory = [self getAPIFactory];
    id<BTFuseFilesystemFSAPIProto> fsapi = [factory get: url];
    
    BTFuseError* error = nil;
    bool exists = [fsapi exists: url error: &error];
    if (error != nil) {
        [response sendError: error];
        return;
    }
    
    if (exists) {
        [response sendString: @"true"];
    }
    else {
        [response sendString: @"false"];
    }
}

@end
