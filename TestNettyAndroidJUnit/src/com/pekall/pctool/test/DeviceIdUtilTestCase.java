package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.util.DeviceInfoUtil;
import com.pekall.pctool.util.Slog;

public class DeviceIdUtilTestCase extends AndroidTestCase {
    public void testGetBuildSerial() throws Exception {
        Slog.d("build serial: " + DeviceInfoUtil.getBuildSerial());
    }
    
    public void testGetImei() throws Exception {
        Slog.d("imei: " + DeviceInfoUtil.getImei(getContext()));
    }
    
    public void testGetId() throws Exception {
        Slog.d("id: " + DeviceInfoUtil.getDeviceUuid(getContext()));
    }
}
