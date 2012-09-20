package com.pekall.pctool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.util.Base64;

public class WifiUtil {
	public static byte[] hostPartInt2Bytes(int address) {
		Stack<Byte> bytes = new Stack<Byte>();
        for (int k = 0; k < 4; k++) {
            byte part = (byte) ((address >> k * 8) & 0xFF);
            if (part != 0) {
            	bytes.push(part);
            }
        }
        
        byte[] ret = new byte[bytes.size()];
        
        for (int i = 0; i < ret.length; i++) {
        	ret[i] = bytes.pop();
        }
        
        return ret; 
	}
	
	public static int hostPartBytesToInt(byte[] address) {
		int host = 0;
        for (int i = 0; i < 4; i++) {
            if (i < address.length) {
                host = (host << 8) | address[i];
            } else {
                host = (host << 8);
            }
        }
        return host;
	}
	
    public static byte[] getWifiAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        
        DhcpInfo dhcp = wifiManager.getDhcpInfo();
        if (dhcp == null) {
            Slog.d("Could not get dhcp info");
            return null;
        }

        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((dhcp.ipAddress >> k * 8) & 0xFF);
        }
        return quads; 
    }
    
    public static String getWifiAddressBase64(Context context) {
        byte[] address = getWifiAddress(context);
        
        return Base64.encodeToString(address, Base64.DEFAULT);
    }
    
    public static byte[] getWifiHostAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifiManager.getDhcpInfo();

        int host = (dhcp.ipAddress & ~dhcp.netmask);
        
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((host >> k * 8) & 0xFF);
        }
        return quads;
    }
    
    public static String getWifiHostAddressBase64(Context context) {
        byte[] address = getWifiHostAddress(context);
        
        List<Byte> input = new ArrayList<Byte>();
        for (byte segment : address) {
            if (segment > 0) {
                input.add(segment);
            }
        }
        
        byte[] inputBytes = new byte[input.size()];
        
        for (int i = 0; i < inputBytes.length; i++) {
            inputBytes[i] = input.get(i);
        }
        
        return Base64.encodeToString(inputBytes, Base64.DEFAULT).trim();
    }
    
    public static int decodeWifiHostAddressBase64(String base64Str) {
        byte[] bytes = Base64.decode(base64Str, Base64.DEFAULT);
        Slog.d(Arrays.toString(bytes));
        final int length = bytes.length;
        int host = 0;
        for (int i = 0; i < 4; i++) {
            if (i < bytes.length) {
                host = (host << 8) | bytes[length - i];
            } else {
                host = (host << 8);
            }
        }
        return host;
    }
    
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        State wifiState = wifiInfo.getState();
        return (wifiState == State.CONNECTED);
    }
}
