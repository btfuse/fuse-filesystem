package com.breautek.fuse.filesystem;

import android.net.Uri;

import com.breautek.fuse.FuseError;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class FSAPI implements IFSAPI {
    public static final String ERROR_TAG = "FuseFilesystem";

    private String $parseUri(Uri uri) {
        return uri.getPath();
    }

    public long append(Uri uri, InputStream io, long contentLength, int chunkSize) throws FuseError {
        String path = $parseUri(uri);
        File file = new File(path);

        if (contentLength == 0) {
            return 0;
        }

        long bytesWritten = 0;
        try (FileOutputStream ostream = new FileOutputStream(file, true)) {
            if (chunkSize > contentLength) {
                chunkSize = (int) contentLength;
            }

            byte[] buffer = new byte[chunkSize];
            long totalBytesRead = 0;
            int bytesRead = 0;
            while (true) {
                long bytesToRead = contentLength - totalBytesRead;

                if (bytesToRead < chunkSize) {
                    buffer = new byte[bytesRead];
                }

                bytesRead = io.read(buffer);

                if (bytesRead == -1) {
                    break;
                }

                totalBytesRead += bytesRead;
                ostream.write(buffer);
                bytesWritten += buffer.length;

                if (totalBytesRead == contentLength) {
                    break;
                }
            }
        }
        catch (FileNotFoundException ex) {
            throw new FuseError(ERROR_TAG, 0, "No such file found at \"" + path + "\"", ex);
        }
        catch (IOException ex) {
            throw new FuseError(ERROR_TAG, 0, "IO Exception while appending data", ex);
        }

        return bytesWritten;
    }

    public boolean delete(Uri uri, boolean recursive) throws FuseError {
        String path = $parseUri(uri);
        File file = new File(path);

        if (!file.exists()) {
            return false;
        }

        boolean didDelete = false;
        if (recursive) {
            didDelete = FileUtils.deleteRecursively(file);
        }
        else {
            didDelete = file.delete();
        }

        return didDelete;
    }

    public FuseFileType getType(Uri uri) throws FuseError {
        String path = $parseUri(uri);
        File file = new File(path);

        if (!file.exists()) {
            throw new FuseError("FuseFilesystem", 0, "No such file found at \"" + path + "\"");
        }

        FuseFileType type = null;
        if (file.isFile()) {
            type = FuseFileType.FILE;
        }
        else if (file.isDirectory()) {
            type = FuseFileType.DIRECTORY;
        }

        return type;
    }

    public boolean exists(Uri uri) throws FuseError {
        File file = new File($parseUri(uri));
        return file.exists();
    }

    public long getSize(Uri uri) throws FuseError {
        String path = $parseUri(uri);
        File file = new File(path);

        if (!file.exists()) {
            throw new FuseError("FuseFilesystem", 0, "No such file found at \"" + path + "\"");
        }

        return file.length();
    }

    public boolean mkdir(Uri uri, boolean recursive) throws FuseError {
        String path = $parseUri(uri);
        File file = new File(path);

        boolean didCreate;
        try {
            if (recursive) {
                didCreate = file.mkdirs();
            } else {
                didCreate = file.mkdir();
            }
        }
        catch (SecurityException error) {
            throw new FuseError("FuseFileystem", 0, "Permission denied.", error);
        }

        return didCreate;
    }
}
