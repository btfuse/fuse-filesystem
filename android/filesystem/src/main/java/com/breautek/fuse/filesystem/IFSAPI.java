package com.breautek.fuse.filesystem;

import android.net.Uri;

import com.breautek.fuse.FuseError;

import java.io.InputStream;

public interface IFSAPI {
    long append(Uri uri, InputStream io, long contentLength, int chunkSize) throws FuseError;
}
