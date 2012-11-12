
package com.pekall.pctool.model.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.pekall.pctool.util.Slog;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class AppUtil {
    
    private AppUtil() {}
    
    public static class AppNotExistException extends Exception {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private String mPackageName;

        public AppNotExistException(String packageName) {
            super();
            mPackageName = packageName;
        }

        @Override
        public String toString() {
            return "AppNotExistException [mPackageName=" + mPackageName + "]";
        }
    }

    public static interface AppFilter {
        public boolean filterApp(ApplicationInfo info);
    }

    private static final AppFilter APP_TYPE_USER_FILTER = new AppFilter() {
        @Override
        public boolean filterApp(ApplicationInfo info) {
            return (info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                    || (info.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
        }
    };

    private static final int getAppType(ApplicationInfo info) {
        if ((info.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
                || (info.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
            return AppInfo.FLAG_APP_TYPE_USER;
        } else {
            return AppInfo.FLAG_APP_TYPE_SYSTEM;
        }
    }

    private static final int getAppInstallLocation(ApplicationInfo info) {
        if ((info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) != 0) {
            return AppInfo.FLAG_INSTALL_LOCATION_EXTERNAL;
        } else {
            return AppInfo.FLAG_INSTALL_LOCATION_INTERNAL;
        }
    }

    public static List<AppInfo> getAppInfos(Context context) {
        List<AppInfo> appInfos = new ArrayList<AppInfo>();

        PackageManager pm = context.getPackageManager();
        List<PackageInfo> infos = pm.getInstalledPackages(0);

        Slog.d("infos size = " + infos.size());
        
        String selfPackageName = context.getPackageName();
        
        for (PackageInfo info : infos) {
            
            // note: don't include self package into app list
            if (info.packageName.equals(selfPackageName)) {
                Slog.d("skip self package name: " + selfPackageName);
                continue;
            }
            
            ApplicationInfo applicationInfo = info.applicationInfo;
            AppInfo appInfo = new AppInfo();

            appInfo.label = applicationInfo.loadLabel(pm).toString();

            // FIXME: do not include icon temporarily
            //
            //appInfo.icon = drawableToBytes(applicationInfo.loadIcon(pm));
            
            appInfo.packageName = info.packageName;
            appInfo.versionName = info.versionName;
            appInfo.versionCode = info.versionCode;
            File apkFile = new File(applicationInfo.sourceDir);
            appInfo.apkFilePath = apkFile.getAbsolutePath();
            appInfo.apkFileSize = apkFile.length();
            appInfo.appType = getAppType(applicationInfo);
            appInfo.installLocation = getAppInstallLocation(applicationInfo);

            appInfos.add(appInfo);
        }
        return appInfos;
    }

    /**
     * Get application apk file path
     * 
     * @param context
     * @param packageName
     * @return the application apk file path or null if the application's apk
     *         file not found
     */
    public static String getAppApkFilePath(Context context, String packageName) throws AppNotExistException {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo packageInfo = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            return applicationInfo.sourceDir;
        } catch (NameNotFoundException e) {
            Slog.e("Cannot find package " + packageName, e);
            throw new AppNotExistException(packageName);
        }
    }
    
    
    public static InputStream getAppApkStream(Context context, String packageName) throws AppNotExistException {
        String filePath = null;
        try {
            filePath = getAppApkFilePath(context, packageName);
            return context.getContentResolver().openInputStream(Uri.parse("file://" + filePath));
        } catch (FileNotFoundException e) {
            Slog.e("file not found: " + filePath, e);
            throw new AppNotExistException(packageName);
        }
    }
    
    public static void installAPK(Context context, File file) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }
    
    public static void uninstallAPK(Context context, String packageName) {
        Uri packageURI = Uri.parse("package:" + packageName);   
        Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);   
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static final String PC_TOOL_EXPORT_APP_DIR = "/sdcard/pctool";
    
    public static boolean exportApp(Context context, String packageName) throws AppNotExistException {
        File file = new File(PC_TOOL_EXPORT_APP_DIR);
        if (!file.exists()) {
            boolean ret = file.mkdirs();
            Slog.d("Export dir not exist, create ret = " + ret);
        }
        
        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = getAppApkStream(context, packageName);
            os = new FileOutputStream(PC_TOOL_EXPORT_APP_DIR + File.separator + packageName + ".apk");
            
            if (is instanceof FileInputStream) {
                Slog.d("use new io");
                
                FileChannel src = ((FileInputStream)is).getChannel();
                FileChannel dst = os.getChannel();
                
                dst.transferFrom(src, 0, src.size());
            } else {
                Slog.d("use old io");
                
                byte[] buffer = new byte[1024];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            }
            return true;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        } finally {
        	if (is != null) {
        		try {
					is.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        	
        	if (os != null) {
        		try {
					os.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}
        }
        
    }
    
    public static boolean importApp(Context context, String packageName) {
        // TODO:
        return true;
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }
}
