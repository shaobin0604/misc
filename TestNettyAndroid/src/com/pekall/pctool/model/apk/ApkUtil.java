package com.pekall.pctool.model.apk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

public class ApkUtil {
    
    public static interface AppFilter {
        public boolean filterApp(ApplicationInfo info);
    }
    
    private static final AppFilter THIRD_PARTY_FILTER = new AppFilter() {
        @Override
        public boolean filterApp(ApplicationInfo info) {
            return (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 || (info.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
        }
    };
    
    public static List<ApkInfo> getApkInfos(Context context) {
        List<ApkInfo> apkInfos = new ArrayList<ApkInfo>();
        
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> infos = pm.getInstalledPackages(0);
        for (PackageInfo info : infos) {
            ApplicationInfo applicationInfo = info.applicationInfo;
            if (THIRD_PARTY_FILTER.filterApp(applicationInfo)) {
                ApkInfo apkInfo = new ApkInfo();
                
                apkInfo.displayName = applicationInfo.loadLabel(pm).toString();
                apkInfo.packageName = info.packageName;
                apkInfo.versionName = info.versionName;
                apkInfo.versionCode = info.versionCode;
            }
        }
        return apkInfos;
        
    }
}
