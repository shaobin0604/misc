package com.pekall.pctool.util;

import android.os.Environment;

import java.io.File;

public class StorageUtil {
    
    public static boolean isExternalStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    
    public static String absolutePathToRelativePath(String absolutePath) {
        File externalDir = Environment.getExternalStorageDirectory();
        return absolutePath.replace(externalDir.getAbsolutePath(), "");
    }
}
