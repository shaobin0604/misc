package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.Slog;
import com.pekall.pctool.WifiModeUtil;

import java.util.Arrays;

public class WifiUtilTestCase extends AndroidTestCase {

    public void testGetWifiAddress() throws Exception {
        Slog.d(Arrays.toString(WifiModeUtil.getWifiAddress(getContext())));
    }
    
    public void testGetWifiAddressBase64() throws Exception {
        Slog.d(WifiModeUtil.getWifiAddressBase64(getContext()));
    }
    
    public void testGetWifiHostAddress() throws Exception {
        Slog.d(Arrays.toString(WifiModeUtil.getWifiHostAddress(getContext())));
    }
    
    public void testGetWifiHostAddressBase64() throws Exception {
        final String wifiHostAddressBase64 = WifiModeUtil.getWifiHostAddressBase64(getContext());
        Slog.d(wifiHostAddressBase64);
        int wifiHostAddress = WifiModeUtil.decodeWifiHostAddressBase64(wifiHostAddressBase64);
        Slog.d("wifihostAddress: " + wifiHostAddress);
    }
}
