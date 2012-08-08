package com.pekall.pctool.test;

import java.util.List;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.app.AppInfo;
import com.pekall.pctool.model.app.AppUtil;

import android.test.AndroidTestCase;

public class AppUtilTestCase extends AndroidTestCase {
	
	public void testGetApkInfos() {
		List<AppInfo> appInfos = AppUtil.getAppInfos(getContext());
		
		assertTrue(appInfos.size() > 0);
		for (AppInfo appInfo : appInfos) {
			Slog.d(appInfo.toString());
		}
	}
}
