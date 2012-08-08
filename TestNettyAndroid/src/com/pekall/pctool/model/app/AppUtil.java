package com.pekall.pctool.model.app;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.pekall.pctool.Slog;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

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
                
                appInfo.displayName = applicationInfo.loadLabel(pm).toString();
                appInfo.icon = drawableToBytes(applicationInfo.loadIcon(pm));
                appInfo.packageName = info.packageName;
                appInfo.versionName = info.versionName;
                appInfo.versionCode = info.versionCode;
                appInfo.apkFile = new File(applicationInfo.sourceDir);
                appInfo.size = appInfo.apkFile.length();
                
                appInfos.add(appInfo);
            }
        }
        return appInfos;
        
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
