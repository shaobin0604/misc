package com.pekall.pctool.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

public class VersionUtil {
    
    private static String sVersionName;
    private static int sVersionCode;
    
    public static int getVersionCode(Context context) {
        if (sVersionCode == 0) {
            getVersionInfo(context);
        }
        return sVersionCode;
    }
    
    public static String getVersionName(Context context) {
        if (sVersionName == null) {
            getVersionInfo(context);
        }
        return sVersionName;
    }
    
    private static void getVersionInfo(Context context) {
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo info = packageManager.getPackageInfo(context.getPackageName(), 0);

            sVersionName = info.versionName;
            sVersionCode = info.versionCode;
        } catch (NameNotFoundException e) {
            // the current package should be exist all times
            Slog.e("the current package should be exist all times", e);
        }
    }
}
