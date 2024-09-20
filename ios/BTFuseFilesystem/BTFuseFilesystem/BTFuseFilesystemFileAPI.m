
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
#import <BTFuseFilesystem/BTFuseFilesystemFileAPI.h>
#import "BTFuseFilesystem/BTFuseFilesystemVars.h"

@implementation BTFuseFilesystemFileAPI {}

- (NSString*) $parseURL:(NSURL*) url {
    return url.path;
}

- (NSNumber*) $getFileSize:(NSString*) path error:(BTFuseError**) error {
    NSFileManager* fm = [NSFileManager defaultManager];
    
    BOOL isDirectory;
    BOOL exists = [fm fileExistsAtPath: path isDirectory: &isDirectory];
    
    if (!exists) {
        NSString* message = [NSString stringWithFormat:@"No such file found at \"%@\"", path];
        *error = [[BTFuseError alloc] init:@"FuseFilesystem" withCode:0 withMessage:message];
        return nil;
    }
    
    if (isDirectory) {
        NSString* message = [NSString stringWithFormat:@"Path \"%@\" is a directory", path];
        *error = [[BTFuseError alloc] init:@"FuseFilesystem" withCode:0 withMessage:message];
        return nil;
    }
    
    NSError* e;
    NSDictionary* attributes = [fm attributesOfItemAtPath: path error: &e];
    
    if (e != nil) {
        *error = [[BTFuseError alloc] init:@"FuseFilesystem" withCode:0 withError: e];
        return nil;
    }
    
    if (attributes == nil) {
        NSString* message = [NSString stringWithFormat:@"Could not read \"%@\"", path];
        *error = [[BTFuseError alloc] init:@"FuseFilesystem" withCode:0 withMessage:message];
        return nil;
    }
    
    return [attributes objectForKey:NSFileSize];
}

- (long) append:(NSURL*) url input:(BTFuseStreamReader*) reader contentLength:(long) contentLength chunkSize:(uint32_t) chunkSize error:(BTFuseError**) outError {
    NSString* path = [self $parseURL: url];
    
    NSFileHandle* fileHandle = [NSFileHandle fileHandleForWritingAtPath: path];
    if (!fileHandle) {
        *outError = [[BTFuseError alloc] init:BTFUSE_FILESYSTEM_TAG withCode:0 withMessage: [NSString stringWithFormat: @"Failed to open path \"%@\"", path]];
        return 0;
    }
    
    NSError* fileError = nil;
    
    [fileHandle seekToEndReturningOffset:nil error:&fileError];
    if (fileError != nil) {
        *outError = [[BTFuseError alloc] init:BTFUSE_FILESYSTEM_TAG withCode: 0 withError: fileError];
        return 0;
    }

    if (contentLength == 0) {
        [fileHandle closeFile];
        return 0;
    }
    
    if (chunkSize > contentLength) {
        chunkSize = (uint32_t) contentLength;
    }
    
    uint8_t buffer[chunkSize];
    uint64_t totalBytesRead = 0;
    uint64_t bytesWritten = 0;
    NSInteger bytesRead = 0;
    bool didError = false;
    
    while(true) {
        uint64_t totalBytesToRead = contentLength - totalBytesRead;
        
        if (totalBytesToRead == 0) {
            break;
        }
        
        uint32_t bytesToRead = 0;
        if (totalBytesToRead > UINT32_MAX) {
            bytesToRead = UINT32_MAX;
        }
        else {
            bytesToRead = (uint32_t) totalBytesToRead;
        }
        
        if (bytesToRead > chunkSize) {
            bytesToRead = chunkSize;
        }
        
        bytesRead = [reader read: buffer maxBytes: bytesToRead];
        
        if (bytesRead == -1) {
            break;
        }
        
        NSData* data = [[NSData alloc] initWithBytes: buffer length: bytesRead];
        totalBytesRead += bytesRead;
        [fileHandle writeData: data error: &fileError];
        bytesWritten += [data length];
        
        if (fileError != nil) {
            didError = true;
            break;
        }
    }
    
    [fileHandle closeFile];
    
    if (didError) {
        *outError = [[BTFuseError alloc] init:BTFUSE_FILESYSTEM_TAG withCode: 0 withError: fileError];
        return 0;
    }
    
    return 0;
}

- (bool) delete:(NSURL*) url recursive:(bool) recursive error:(BTFuseError**) outError {
    NSString* path = [self $parseURL: url];
    
    BTFuseError* error = nil;
    bool exists = [self exists:url error:&error];
    if (error != nil) {
        *outError = error;
        return false;
    }
    
    if (!exists) {
        return false;
    }
    
    NSFileManager* fm = [NSFileManager defaultManager];
    NSError* fileError = nil;
    [fm removeItemAtPath: path error: &fileError];
    
    if (fileError != nil) {
        *outError = [[BTFuseError alloc] init:BTFUSE_FILESYSTEM_TAG withCode: 0 withError: fileError];
        return false;
    }
    
    // Unlike the Android APIs, we don't know if things were actually deleted, however,
    // it's likely safe to assume if we made it here without error, then the object at path
    // was in fact, removed.
    return true;
}

- (bool) exists:(NSURL*) url error:(BTFuseError **) outError {
    NSString* path = [self $parseURL: url];
    NSFileManager* fm = [NSFileManager defaultManager];
    
    BOOL isDirectory;
    return [fm fileExistsAtPath: path isDirectory: &isDirectory];
}

- (long) getSize:(NSURL*) url error:(BTFuseError**) outError {
    NSString* path = [self $parseURL: url];
    
    BTFuseError* error = nil;
    NSNumber* size = [self $getFileSize: path error: &error];
    
    if (error != nil) {
        *outError = error;
        return -1;
    }
    
    return [size longValue];
}

- (BTFuseFilesystemFileType) getType:(NSURL*) url error:(BTFuseError**) outError {
    NSString* path = [self $parseURL: url];
    
    NSFileManager* fm = [NSFileManager defaultManager];
    
    bool isDirectory;
    bool exists = [fm fileExistsAtPath: path isDirectory:&isDirectory];
    
    if (!exists) {
        *outError = [[BTFuseError alloc] init:BTFUSE_FILESYSTEM_TAG withCode: 0 withMessage: [NSString stringWithFormat:@"No such file found at %@", path]];
        return -1;
    }
    
    BTFuseFilesystemFileType type = BTFuseFilesystemFileTypeFile;
    
    if (isDirectory) {
        type = BTFuseFilesystemFileTypeDirectory;
    }
    
    return type;
}

- (bool) mkdir:(NSURL*) url recursive:(bool) recursive error:(BTFuseError**) outError {
    BTFuseError* error = nil;
    bool exists = [self exists: url error: &error];
    
    if (error != nil) {
        *outError = error;
        return false;
    }
    
    if (exists) {
        return false;
    }
    
    NSString* path = [self $parseURL: url];
    
    NSFileManager* fm = [NSFileManager defaultManager];
    NSError* fileError = nil;
    bool didCreate = [fm createDirectoryAtPath: path withIntermediateDirectories: recursive attributes:nil error:&fileError];
    
    if (fileError != nil) {
        *outError = [[BTFuseError alloc] init:BTFUSE_FILESYSTEM_TAG withCode: 0 withError: fileError];
        return false;
    }
    
    return didCreate;
}

- (long) read:(NSURL*) url length:(long) length offset:(long) offset chunkSize:(uint32_t) chunkSize callback:(id<BTFuseFilesystemFSAPIProtoReadCallback>) callback error:(BTFuseError**) outError {
    NSString* path = [self $parseURL: url];
    
    NSFileHandle* fileHandle = [NSFileHandle fileHandleForReadingAtPath: path];
    if (!fileHandle) {
        *outError = [[BTFuseError alloc] init:BTFUSE_FILESYSTEM_TAG withCode: 0 withMessage:[NSString stringWithFormat: @"Failed to open path \"%@\"", path]];
        return -1;
    }
    
    BTFuseError* fuseError = nil;
    long fileSize = [self getSize: url error: &fuseError];
    if (fuseError != nil) {
        *outError = fuseError;
        return -1;
    }
    
    uint64_t contentLength = 0;
    if (length == -1) {
        contentLength = fileSize;
    }
    else {
        contentLength = (length < fileSize) ? length : fileSize;
    }
    
    if (contentLength + offset > fileSize) {
        contentLength -= (contentLength + offset) - fileSize;
    }
    
    if (contentLength == 0) {
        [callback onReadStart: 0];
        [callback onReadClose];
        return 0;
    }
    
    if (chunkSize > contentLength) {
        chunkSize = (uint32_t) contentLength;
    }
    
    NSError* error = nil;
    uint64_t totalBytesRead = 0;
    
    if (offset > 0) {
        [fileHandle seekToOffset: offset error: &error];
        if (error != nil) {
            *outError = [[BTFuseError alloc] init:BTFUSE_FILESYSTEM_TAG withCode:0 withError:error];
            return totalBytesRead;
        }
    }
    
    [callback onReadStart: contentLength];
    
    bool didFail = false;
    while (totalBytesRead < contentLength) {
        NSData* data = [fileHandle readDataOfLength: chunkSize];
        if (data == nil) {
            didFail = true;
            break;
        }
        
        totalBytesRead += [data length];
        [callback onReadChunk: data];
    }
    
    [fileHandle closeFile];
    
    if (didFail) {
        [callback onReadFailure: [[BTFuseError alloc] init:BTFUSE_FILESYSTEM_TAG withCode: 0 withMessage: @"Failed to read data"]];
        return totalBytesRead;
    }
    
    return totalBytesRead;
}

- (long) truncate:(NSURL*) url contentLength:(long) contentLength input:(BTFuseStreamReader*) reader chunkSize:(uint32_t) chunkSize error:(BTFuseError**) outError {
    NSString* path = [self $parseURL: url];
    
    NSFileHandle* fileHandle = [NSFileHandle fileHandleForWritingAtPath: path];
    
    if (!fileHandle) {
        *outError = [[BTFuseError alloc] init:BTFUSE_FILESYSTEM_TAG withCode:0 withMessage: [NSString stringWithFormat: @"Failed to open path \"%@\"", path]];
        return -1;
    }
    
    NSError* fileError = nil;
    
    [fileHandle truncateAtOffset: 0 error: &fileError];
    if (fileError != nil) {
        *outError = [[BTFuseError alloc] init:BTFUSE_FILESYSTEM_TAG withCode: 0 withError: fileError];
        return -1;
    }
    
    if (contentLength == 0) {
        [fileHandle closeFile];
        return 0;
    }
    
    if (chunkSize > contentLength) {
        chunkSize = (uint32_t) contentLength;
    }
    
    uint8_t buffer[chunkSize];
    uint64_t totalBytesRead = 0;
    long bytesWritten = 0;
    NSInteger bytesRead = 0;
    bool didError = false;
    
    while (true) {
        uint64_t totalBytesToRead = contentLength - totalBytesRead;
        
        if (totalBytesToRead == 0) {
            break;
        }
        
        uint32_t bytesToRead = 0;
        if (totalBytesToRead > UINT32_MAX) {
            bytesToRead = UINT32_MAX;
        }
        else {
            bytesToRead = (uint32_t) totalBytesToRead;
        }
        
        if (bytesToRead > chunkSize) {
            bytesToRead = chunkSize;
        }
        
        bytesRead = [reader read: buffer maxBytes: bytesToRead];
        
        if (bytesRead == -1) {
            break;
        }
        
        NSData* data = [[NSData alloc] initWithBytes: buffer length: bytesRead];
        totalBytesRead += bytesRead;
        [fileHandle writeData: data error: &fileError];
        bytesWritten += [data length];
        
        if (fileError != nil) {
            didError = true;
            break;
        }
    }
    
    [fileHandle closeFile];
    
    if (didError) {
        *outError = [[BTFuseError alloc] init:BTFUSE_FILESYSTEM_TAG withCode: 0 withError: fileError];
        return bytesWritten;
    }
    
    return bytesWritten;
}

- (long) write:(NSURL*) url offset:(long) offset chunkSize:(uint32_t) chunkSize input:(BTFuseStreamReader*) reader contentLength:(long) contentLength error:(BTFuseError**) outError {
    
    NSString* path = [self $parseURL: url];
    NSFileHandle* fileHandle = [NSFileHandle fileHandleForWritingAtPath: path];
    
    if (!fileHandle) {
        *outError = [[BTFuseError alloc] init:BTFUSE_FILESYSTEM_TAG withCode:0 withMessage: [NSString stringWithFormat: @"Failed to open path \"%@\"", path]];
        return -1;
    }
    
    NSError* fileError = nil;
    if (offset != 0) {
        [fileHandle seekToOffset: offset error: &fileError];
        if (fileError != nil) {
            *outError = [[BTFuseError alloc] init: BTFUSE_FILESYSTEM_TAG withCode: 0 withError: fileError];
            return -1;
        }
    }
    
    if (chunkSize > contentLength) {
        chunkSize = (uint32_t) contentLength;
    }
    
    uint8_t buffer[chunkSize];
    uint64_t totalBytesRead = 0;
    uint64_t bytesWritten = 0;
    NSInteger bytesRead = 0;
    bool didError = false;
    
    while(true) {
        uint64_t totalBytesToRead = contentLength - totalBytesRead;
        
        if (totalBytesToRead == 0) {
            break;
        }
        
        uint32_t bytesToRead = 0;
        if (totalBytesToRead > UINT32_MAX) {
            bytesToRead = UINT32_MAX;
        }
        else {
            bytesToRead = (uint32_t) totalBytesToRead;
        }
        
        if (bytesToRead > chunkSize) {
            bytesToRead = chunkSize;
        }
        
        bytesRead = [reader read: buffer maxBytes: bytesToRead];
        
        if (bytesRead == -1) {
            break;
        }
        
        NSData* data = [[NSData alloc] initWithBytes: buffer length: bytesRead];
        totalBytesRead += bytesRead;
        [fileHandle writeData: data error: &fileError];
        bytesWritten += [data length];
        
        if (fileError != nil) {
            didError = true;
            break;
        }
    }
    
    [fileHandle closeFile];
    
    if (didError) {
        *outError = [[BTFuseError alloc] init:BTFUSE_FILESYSTEM_TAG withCode: 0 withError: fileError];
        return -1;
    }
    
    return bytesWritten;
}

@end
