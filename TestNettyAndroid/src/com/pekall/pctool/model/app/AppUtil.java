package com.pekall.pctool.model.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import com.google.protobuf.ByteString;
import com.pekall.pctool.Slog;
import com.pekall.pctool.protos.AppInfoProtos.AppInfoP;
import com.pekall.pctool.protos.AppInfoProtos.AppInfoPList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AppUtil {
    
    public static interface AppFilter {
        public boolean filterApp(ApplicationInfo info);
    }
    
    private static final AppFilter THIRD_PARTY_FILTER = new AppFilter() {
        @Override
        public boolean filterApp(ApplicationInfo info) {
            return (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0 || (info.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
        }
    };
    
    public static List<AppInfo> getAppInfos(Context context) {
        List<AppInfo> appInfos = new ArrayList<AppInfo>();
        
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> infos = pm.getInstalledPackages(0);
        
        Slog.d("infos size = " + infos.size());
        
        for (PackageInfo info : infos) {
            ApplicationInfo applicationInfo = info.applicationInfo;
            if (THIRD_PARTY_FILTER.filterApp(applicationInfo)) {
                AppInfo appInfo = new AppInfo();
                
                appInfo.label = applicationInfo.loadLabel(pm).toString();
                appInfo.icon = drawableToBytes(applicationInfo.loadIcon(pm));
                appInfo.packageName = info.packageName;
                appInfo.versionName = info.versionName;
                appInfo.versionCode = info.versionCode;
                File apkFile = new File(applicationInfo.sourceDir);
                appInfo.apkFilePath = apkFile.getAbsolutePath();
                appInfo.apkFileSize = apkFile.length();
                
                appInfos.add(appInfo);
            }
        }
        return appInfos;
    }

    public static AppInfoPList getAppInfoPList(Context context) {
        AppInfoPList.Builder listBuilder = AppInfoPList.newBuilder();
        
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> infos = pm.getInstalledPackages(0);
        
        Slog.d("infos size = " + infos.size());
        
        for (PackageInfo info : infos) {
            ApplicationInfo applicationInfo = info.applicationInfo;
            if (THIRD_PARTY_FILTER.filterApp(applicationInfo)) {
                AppInfoP.Builder itemBuilder = AppInfoP.newBuilder();
                
                itemBuilder.setLabel(applicationInfo.loadLabel(pm).toString());
                itemBuilder.setIcon(ByteString.copyFrom(drawableToBytes(applicationInfo.loadIcon(pm))));
                itemBuilder.setPackageName(info.packageName);
                itemBuilder.setVersionName(info.versionName);
                itemBuilder.setVersionCode(info.versionCode);

                File apkFile = new File(applicationInfo.sourceDir);
                itemBuilder.setApkFileSize(apkFile.length());
                itemBuilder.setApkFilePath(apkFile.getAbsolutePath());
                
                listBuilder.addAppInfos(itemBuilder);
            }
        }
        return listBuilder.build();
    }
    
    /**
     * Get application apk file path
     * 
     * @param context
     * @param packageName
     * @return the application apk file path or null if the application's apk file not found
     */
    public static String getAppApkFilePath(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        String apkPath = null;
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            apkPath = applicationInfo.sourceDir;
        } catch (NameNotFoundException e) {
            Slog.e("Cannot find package " + packageName, e);
        }
        return apkPath;
    }
    
    private static byte[] drawableToBytes(Drawable drawable) {
    	Bitmap bitmap;
    	if (drawable instanceof BitmapDrawable) {
    		bitmap = ((BitmapDrawable) drawable).getBitmap();
    	} else {
    		bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
    	    Canvas canvas = new Canvas(bitmap); 
    	    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
    	    drawable.draw(canvas);
    	}
    	
    	return bitmapToBytes(bitmap);
    }
    
    private static byte[] bitmapToBytes(Bitmap bitmap) {
    	ByteArrayOutputStream stream = new ByteArrayOutputStream();
    	bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
    	return stream.toByteArray();
    }
}
