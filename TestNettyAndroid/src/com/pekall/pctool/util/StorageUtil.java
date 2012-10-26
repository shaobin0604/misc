package com.pekall.pctool.util;

import android.os.Environment;

public class StorageUtil {
    
    public static boolean isSdCardMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
