
package com.pekall.backup;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.pekall.backup.backupservicetestapp.Slog;

public class Backup {

    private int mNativeContext;

    private Context mContext;

    static {
        System.loadLibrary("backup_jni");
        native_init();
    }

    public Backup(Context context) {
        mContext = context;

        /*
         * Native setup requires a weak reference to our object. It's easier to
         * create it here than in C++.
         */
        native_setup(new WeakReference<Backup>(this));
    }

    public void release() {
        native_release();
        mContext = null;
    }

    private static native final void native_init();

    private native final void native_setup(Object backup_this);

    private native final void native_release();

    private native final String native_getDirOwnerStr(String packageName);

    /**
     * Get the user name and group name for app package's private data directory
     * 
     * @param packageName
     * @return [user name, group name] pair
     */
    public String[] getPackageUnameGname(String packageName) {
        String dirOwnerStr = native_getDirOwnerStr(packageName);

        Slog.d("dirOwnerStr = " + dirOwnerStr);

        if (dirOwnerStr == null || dirOwnerStr.length() == 0) {
            return null;
        }

        Pattern pattern = Pattern.compile("[drwx\\-]{10}\\s+(\\w+)\\s+(\\w+)");

        Matcher matcher = pattern.matcher(dirOwnerStr);

        String username = null, groupname = null;

        if (matcher.find()) {
            username = matcher.group(1);
            groupname = matcher.group(2);
        }

        if (username == null || groupname == null) {
            return null;
        }

        return new String[] {
                username, groupname
        };
    }

    /**
     * Backup app's private data to dstDir <br/>
     * <br/>
     * <b>NOTICE: </b> this method may cost some time depending on the data size
     * that need to be backup.
     * @param backupDir where the backup file reside
     * @param packageName which package's data need to be backup
     * 
     * @return true if success, otherwise false
     */
    public boolean backupData(String backupDir, String packageName) {
        PackageManager pm = mContext.getPackageManager();
        ApplicationInfo appInfo;
        try {
            appInfo = pm.getApplicationInfo(packageName, PackageManager.GET_SHARED_LIBRARY_FILES);
        } catch (NameNotFoundException e) {
            Slog.e("Error: cannot find pacakge: " + packageName, e);
            return false;
        }

        String nativeLibraryDir = appInfo.nativeLibraryDir;
        String dataDir = appInfo.dataDir;
        String sourceDir = appInfo.sourceDir;
        String[] sharedLibraryFiles = appInfo.sharedLibraryFiles;

        Slog.d(String.format("nativeLibraryDir: %s, sharedLibraryFiles: %s, dataDir: %s, sourceDir: %s",
                nativeLibraryDir, Arrays.toString(sharedLibraryFiles), dataDir, sourceDir));

        return native_backupData(packageName, backupDir);
    }

    private native boolean native_backupData(String packageName, String backupDir);

    /**
     * Restore app's private data from srcDir to system <br/>
     * <br/>
     * <b>NOTICE: </b> this method may cost some time depending on the data size
     * that need to be restored.
     * 
     * @param backupDir where the backup files reside
     * @param packageName which package's data need to be restored
     * @return true if success, otherwise false
     */
    public boolean restoreData(String backupDir, String packageName) {
        int packageUid = getPackageOwnerUid(packageName);
        int systemUid = getSystemUid();
        
        if (packageUid == -1 || systemUid == -1) {
            return false;
        }
        
        return native_restoreData(backupDir, packageName, packageUid, systemUid, true);
    }

    /**
     * Get package owner's uid
     * 
     * @param packageName
     * @return the uid of the package owner or <b>-1</b> if the package is not found
     */
    private int getPackageOwnerUid(String packageName) {
        PackageManager pm = mContext.getPackageManager();
        ApplicationInfo appInfo;
        try {
            appInfo = pm.getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            Slog.e("Error: package not found: " + packageName, e);
            return -1;
        }
        int uid = appInfo.uid;
        Slog.d("packageName: " + packageName + ", uid: " + uid);
        return uid;
    }

    /**
     * Get the uid of <b>system</b> user
     * 
     * @return the uid of <b>system</b> user
     */
    private int getSystemUid() {
        return getPackageOwnerUid("com.android.settings");
    }

    private native boolean native_restoreData(String backupDir, String packageName, int packageUid, int systemUid,
            boolean overwrite);
}
