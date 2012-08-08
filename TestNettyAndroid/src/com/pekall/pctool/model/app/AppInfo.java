package com.pekall.pctool.model.app;

import java.io.File;

public class AppInfo {
	public String displayName;
	public String packageName;
	public String versionName;
	public int versionCode;
	public byte[] icon;
	public long size;
	public File apkFile;

	@Override
	public String toString() {
		return "AppInfo [displayName=" + displayName + ", packageName="
				+ packageName + ", versionName=" + versionName
				+ ", versionCode=" + versionCode + ", size=" + size
				+ ", apkFile=" + apkFile + "]";
	}
}
