package com.pekall.pctool;

import android.os.Environment;

public class StorageUtil {
    
    public static boolean isSdCardMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
}
