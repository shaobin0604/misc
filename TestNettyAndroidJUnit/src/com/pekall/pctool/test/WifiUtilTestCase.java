package com.pekall.pctool.test;

import android.test.AndroidTestCase;

import com.pekall.pctool.Slog;
import com.pekall.pctool.WifiUtil;

import java.util.Arrays;

public class WifiUtilTestCase extends AndroidTestCase {
	
	public void testHostPartInt2Bytes() throws Exception {
		byte[] original = {123, 43};
		
		int intValue = WifiUtil.hostPartBytesToInt(original);
		
		Slog.d("intValue: " + intValue);
		
		byte[] transcode = WifiUtil.hostPartInt2Bytes(intValue);
		
		Slog.d("transcode: " + Arrays.toString(transcode));
	}

    public void testGetWifiAddress() throws Exception {
        Slog.d(Arrays.toString(WifiUtil.getWifiAddress(getContext())));
    }
    
    public void testGetWifiAddressBase64() throws Exception {
        Slog.d(WifiUtil.getWifiAddressBase64(getContext()));
    }
    
    public void testGetWifiHostAddress() throws Exception {
        Slog.d(Arrays.toString(WifiUtil.getWifiHostAddress(getContext())));
    }
    
    public void testGetWifiHostAddressBase64() throws Exception {
        final String wifiHostAddressBase64 = WifiUtil.getWifiHostAddressBase64(getContext());
        Slog.d(wifiHostAddressBase64);
        int wifiHostAddress = WifiUtil.decodeWifiHostAddressBase64(wifiHostAddressBase64);
        Slog.d("wifihostAddress: " + wifiHostAddress);
    }
}
