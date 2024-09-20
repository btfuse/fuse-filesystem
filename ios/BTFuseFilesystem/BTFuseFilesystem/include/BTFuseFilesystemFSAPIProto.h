
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

#ifndef BTFuseFilesystemFSAPIProto_h
#define BTFuseFilesystemFSAPIProto_h

#import <Foundation/Foundation.h>
#import <BTFuse/BTFuse.h>
#import <BTFuseFilesystem/BTFuseFilesystemFileType.h>

@protocol BTFuseFilesystemFSAPIProtoReadCallback

- (void) onReadStart:(long) contentLength;
- (void) onReadChunk:(NSData*) chunk;
- (void) onReadClose;
- (void) onReadFailure:(BTFuseError*) error;

@end

@protocol BTFuseFilesystemFSAPIProto

- (long) append:(NSURL*) url input: (BTFuseStreamReader*) io contentLength:(long) contentLength chunkSize: (uint32_t) chunkSize error:(BTFuseError**) outError;
- (bool) delete:(NSURL*) url recursive:(bool) recursive error:(BTFuseError**) outError;
- (BTFuseFilesystemFileType) getType:(NSURL*) url error: (BTFuseError**) outError;
- (bool) exists:(NSURL*) url error: (BTFuseError**) outError;
- (long) getSize:(NSURL*) url error: (BTFuseError**) outError;
- (bool) mkdir:(NSURL*) url recursive:(bool) recursive error:(BTFuseError**) outError;
- (long) read:(NSURL*) url length:(long) length offset:(long) offset chunkSize:(uint32_t) chunkSize callback:(id<BTFuseFilesystemFSAPIProtoReadCallback>) callback error:(BTFuseError**) outError;
- (long) write:(NSURL*) url offset:(long) offset chunkSize:(uint32_t) chunkSize input:(BTFuseStreamReader*) input contentLength:(long) contentLength error:(BTFuseError**) outError;
- (long) truncate:(NSURL*) url contentLength:(long) contentLength input:(BTFuseStreamReader*) input chunkSize:(uint32_t) chunkSize error:(BTFuseError**) outError;

@end

#endif
