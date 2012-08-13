
package com.pekall.pctool.model.app;

public class AppInfo {
    public static final int FLAG_APP_TYPE_SYSTEM = 1;
    public static final int FLAG_APP_TYPE_USER = 2;

    public static final int FLAG_INSTALL_LOCATION_INTERNAL = 1;
    public static final int FLAG_INSTALL_LOCATION_EXTERNAL = 2;

    public String label;
    public String packageName;
    public String versionName;
    public int versionCode;
    public byte[] icon;
    public long apkFileSize;
    public String apkFilePath;
    public int appType;
    public int installLocation;

    @Override
    public String toString() {
        return "AppInfo [label=" + label + ", packageName=" + packageName + ", versionName=" + versionName
                + ", versionCode=" + versionCode + ", apkFileSize=" + apkFileSize + ", apkFilePath=" + apkFilePath
                + ", appType=" + (appType == FLAG_APP_TYPE_SYSTEM ? "SYSTEM" : "USER") + ", installLocation="
                + (installLocation == FLAG_INSTALL_LOCATION_INTERNAL ? "INTERNAL" : "EXTERNAL") + "]";
    }

}
