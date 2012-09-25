package com.pekall.pctool.test;

import java.io.File;
import java.util.List;

import com.pekall.pctool.Slog;
import com.pekall.pctool.model.app.AppInfo;
import com.pekall.pctool.model.app.AppUtil;
import com.pekall.pctool.model.app.AppUtil.AppNotExistException;

import android.test.AndroidTestCase;

public class AppUtilTestCase extends AndroidTestCase {
    
    public void testUninstallAppNotExist() throws Exception {
        String packageName = "com.package.not.exist";
        AppUtil.uninstallAPK(getContext(), packageName);
    }
	
	public void testGetApkInfos() {
		List<AppInfo> appInfos = AppUtil.getAppInfos(getContext());
		assertTrue(appInfos.size() > 0);
		for (AppInfo appInfo : appInfos) {
			Slog.d(appInfo.toString());
		}
	}
	
	public void testGetAppApkFilePath() throws AppNotExistException {
	    String packageName = "cn.yo2.aquarium.callvibrator";
        String actualPath = AppUtil.getAppApkFilePath(getContext(), packageName);
        String expectedPath = "/data/app/cn.yo2.aquarium.callvibrator-1.apk";
        assertEquals(expectedPath, actualPath);
	}
	
	public void testExportAppInternal() throws AppNotExistException {
	    String packageName = "cn.yo2.aquarium.callvibrator";
	    
	    File exported = new File(AppUtil.PC_TOOL_EXPORT_APP_DIR + File.separator + packageName + ".apk");
	    if (exported.exists()) {
	        boolean ret = exported.delete();
	        Slog.d("File exist: " + exported.getName() + ", delete ret = " + ret);
	    }
        
        assertTrue(AppUtil.exportApp(getContext(), packageName));

        assertTrue(exported.exists());
	}
	
	public void testExportAppExternal() throws AppNotExistException {
        String packageName = "com.magmamobile.game.Plumber";
        
        File exported = new File(AppUtil.PC_TOOL_EXPORT_APP_DIR + File.separator + packageName + ".apk");
        if (exported.exists()) {
            boolean ret = exported.delete();
            Slog.d("File exist: " + exported.getName() + ", delete ret = " + ret);
        }
        
        assertTrue(AppUtil.exportApp(getContext(), packageName));

        assertTrue(exported.exists());
    }
}
