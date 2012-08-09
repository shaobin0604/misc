
package com.pekall.pctool.model.app;


public class AppInfo {
    public String label;
    public String packageName;
    public String versionName;
    public int versionCode;
    public byte[] icon;
    public long apkFileSize;
    public String apkFilePath;

    @Override
    public String toString() {
        return "AppInfo [label=" + label + ", packageName=" + packageName + ", versionName=" + versionName
                + ", versionCode=" + versionCode + ", apkFileSize=" + apkFileSize + ", apkFilePath=" + apkFilePath
                + "]";
    }
}
