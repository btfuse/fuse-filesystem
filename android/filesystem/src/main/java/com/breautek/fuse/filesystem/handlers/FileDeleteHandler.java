
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

import android.net.Uri;

import com.breautek.fuse.FuseAPIPacket;
import com.breautek.fuse.FuseAPIResponse;
import com.breautek.fuse.FuseError;
import com.breautek.fuse.FusePlugin.APIHandler;
import com.breautek.fuse.filesystem.FileUtils;
import com.breautek.fuse.filesystem.FuseFilesystemPlugin;
import com.breautek.fuse.filesystem.IFSAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

public class FileDeleteHandler extends APIHandler<FuseFilesystemPlugin> {
    public FileDeleteHandler(FuseFilesystemPlugin plugin) {
        super(plugin);
    }

    @Override
    public void execute(FuseAPIPacket packet, FuseAPIResponse response) throws IOException, JSONException {
        JSONObject params = packet.readAsJSONObject();

        String path = params.getString("path");
        Uri uri = Uri.parse(path);

        boolean recursive = params.getBoolean("recursive");

        IFSAPI fsapi = this.plugin.getFSAPIFactory().get(uri);

        boolean didDelete;
        try {
            didDelete = fsapi.delete(uri, recursive);
        }
        catch (FuseError error) {
            response.send(error);
            return;
        }

        response.send(didDelete ? "true" : "false");
    }
}
