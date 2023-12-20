
/*
Copyright 2023 Breautek

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

package com.breautek.fuse.filesystem.handlers;

import com.breautek.fuse.FuseAPIPacket;
import com.breautek.fuse.FuseAPIResponse;
import com.breautek.fuse.FuseError;
import com.breautek.fuse.FusePlugin.APIHandler;
import com.breautek.fuse.filesystem.FuseFileAPIParams;
import com.breautek.fuse.filesystem.FuseFilesystemPlugin;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class FileAppendHandler extends APIHandler<FuseFilesystemPlugin> {
    public FileAppendHandler(FuseFilesystemPlugin plugin) {
        super(plugin);
    }

    @Override
    public void execute(FuseAPIPacket packet, FuseAPIResponse response) throws IOException {
        FuseFileAPIParams params = FuseFileAPIParams.parse(packet.getContentLength(), packet.getInputStream());
        String path = new String(params.getParams());

        File file = new File(path);

        if (!file.exists()) {
            response.send(new FuseError("FuseFilesystem", 0, "No such file found at \"" + path + "\""));
            return;
        }

        long contentLength = params.getContentLength();

        if (contentLength == 0) {
            response.send("0");
        }

        FileOutputStream io = new FileOutputStream(file, true);

        int bytesWritten = 0;
        try {
                InputStream readStream = packet.getInputStream();
                int chunkSize = this.plugin.getChunkSize();
                if (chunkSize > contentLength) {
                    chunkSize = (int) contentLength;
                }
                byte[] buffer = new byte[chunkSize];
                long totalBytesRead = 0;
                int bytesRead = 0;
                while (true) {
                    long bytesToRead = contentLength - totalBytesRead;
                    if (bytesToRead >= chunkSize) {
                        bytesRead = readStream.read(buffer);
                    } else {
                        buffer = new byte[bytesRead];
                        bytesRead = readStream.read(buffer);
                    }

                    if (bytesRead == -1) {
                        break;
                    }

                    totalBytesRead += bytesRead;
                    io.write(buffer);
                    bytesWritten += buffer.length;

                    if (totalBytesRead == contentLength) {
                        break;
                    }
                }

            io.close();
        }
        catch (Exception e) {
            io.close();
            throw e;
        }

        response.send(Integer.toString(bytesWritten));
    }
}
