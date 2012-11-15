package com.pekall.pctool.util;

import android.os.Environment;
import android.text.TextUtils;

import org.jboss.netty.handler.codec.http.multipart.FileUpload;

import java.io.File;

public class StorageUtil {
    
    public static boolean isExternalStorageMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }
    
    public static File getExternalPictureDir() {
        File externalDir = Environment.getExternalStorageDirectory();
        return new File(externalDir, "Pictures");
    }
    
    public static String absolutePathToRelativePath(String absolutePath) {
        File externalDir = Environment.getExternalStorageDirectory();
        return absolutePath.replace(externalDir.getAbsolutePath(), "");
    }
    
    public static File generateDstFileName(File dir, String filename) {
        File dst = new File(dir, filename);
        
        if (!dst.exists()) {
            return dst;
        }
        
        // file exist, append timestamp
        
        if (!TextUtils.isEmpty(filename)) {
            int idxForExt = filename.lastIndexOf('.');
            if (idxForExt > -1 && idxForExt < filename.length() - 1) {
                
                // has extension
                
                String ext = filename.substring(idxForExt + 1);
                String filenameWithoutExt = filename.substring(0, idxForExt);
                
                return new File(dir, String.format("%s-%d.%s", filenameWithoutExt, System.currentTimeMillis(), ext));
            } else {
                
                return new File(dir, String.format("%s-%d", filename, System.currentTimeMillis()));
            }
        } else {
            return new File(dir, filename);
        }
    }
}
