
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
import com.breautek.fuse.filesystem.FuseFilesystemPlugin;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class FileReadHandler extends APIHandler<FuseFilesystemPlugin> {
    public FileReadHandler(FuseFilesystemPlugin plugin) {
        super(plugin);
    }

    @Override
    public void execute(FuseAPIPacket packet, FuseAPIResponse response) throws IOException, JSONException {
        JSONObject params = packet.readAsJSONObject();

        long desiredLength = params.getLong("length");
        long offset = params.getLong("offset");

        String path = params.getString("path");
        File file = new File(path);

        if (!file.exists()) {
            response.send(new FuseError("FuseFilesystem", 0, "No such file found at \"" + path + "\""));
            return;
        }

        long fileSize = file.length();
        long contentLength = 0;
        if (desiredLength == -1) {
            contentLength = fileSize;
        }
        else {
            contentLength = Math.min(desiredLength, fileSize);
        }

        if (contentLength + offset > fileSize) {
            contentLength -= (contentLength + offset) - fileSize;
        }

        if (contentLength == 0) {
            response.send();
            return;
        }

        int chunkSize = this.plugin.getChunkSize();
        if (chunkSize > contentLength) {
            chunkSize = (int) contentLength;
        }

        response.sendHeaders(200, "application/octet-stream", contentLength);

        long totalBytesRead = 0;
        int bytesRead = 0;
        byte[] buffer = new byte[chunkSize];

        FileInputStream io = new FileInputStream(file);
        try {
            if (offset > 0) {
                io.skip(offset);
            }
            while (totalBytesRead < contentLength && (bytesRead = io.read(buffer)) != -1) {
                if (bytesRead < chunkSize) {
                    byte[] b = new byte[bytesRead];
                    System.arraycopy(buffer, 0, b, 0, bytesRead);
                    response.pushData(b);
                }
                else {
                    response.pushData(buffer);
                }

                totalBytesRead += bytesRead;
            }
        }
        catch (Exception e) {
            io.close();
            throw e;
        }
    }
}
