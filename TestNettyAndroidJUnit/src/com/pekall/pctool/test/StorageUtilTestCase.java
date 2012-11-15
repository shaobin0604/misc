package com.pekall.pctool.test;

import android.os.Environment;
import android.test.AndroidTestCase;

import com.pekall.pctool.util.Slog;
import com.pekall.pctool.util.StorageUtil;

import java.io.File;

public class StorageUtilTestCase extends AndroidTestCase {
    public void testGenerateDstFileNameExt1() throws Exception {
        File extDir = Environment.getExternalStorageDirectory();
        
        File dst = StorageUtil.generateDstFileNameExt(extDir, "README.markdown");
        Slog.d("dst: " + dst);
    }
    
    public void testGenerateDstFileNameExt2() throws Exception {
        File extDir = Environment.getExternalStorageDirectory();
        
        File dst = StorageUtil.generateDstFileNameExt(extDir, "README.");
        Slog.d("dst: " + dst);
    }
}
