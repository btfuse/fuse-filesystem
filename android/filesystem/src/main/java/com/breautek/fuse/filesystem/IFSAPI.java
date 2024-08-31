package com.breautek.fuse.filesystem;

import android.net.Uri;

import com.breautek.fuse.FuseError;

import java.io.InputStream;

public interface IFSAPI {
    long append(Uri uri, InputStream io, long contentLength, int chunkSize) throws FuseError;

    boolean delete(Uri uri, boolean recursive) throws FuseError;

    FuseFileType getType(Uri uri) throws FuseError;

    boolean exists(Uri uri) throws FuseError;

    long getSize(Uri uri) throws FuseError;

    boolean mkdir(Uri uri, boolean recursive) throws FuseError;
}
