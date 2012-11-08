package com.pekall.pctool.util;

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

import org.apache.http.conn.util.InetAddressUtils;

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
	
	public static int getWifiAddressInt(Context context) {
	    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        
        DhcpInfo dhcp = wifiManager.getDhcpInfo();
        return dhcp.ipAddress;
	}
	
    public static byte[] getWifiAddressBytes(Context context) {
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
    
    public static String getWifiAddressRadix36Encoded(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        
        DhcpInfo dhcp = wifiManager.getDhcpInfo();
        if (dhcp == null) {
            Slog.e("Could not get dhcp info");
            return null;
        }
        
        final int ipAddress = dhcp.ipAddress;
        final String radix36Str = Integer.toString(ipAddress, Character.MAX_RADIX);
        
        Slog.d("ipAddress = " + ipAddress + ", radix36Str = " + radix36Str);
        
        return radix36Str;
    }
    
    public static int radix36StrToInt32(String radix36Str)
    {
        int i = 0;

        for (char c : radix36Str.toCharArray())
        {
            i = i * 36 + radix36CharToInt32(c);
        }

        return i;
    }

    private static int radix36CharToInt32(char c)
    {
        if (c >= '0' && c <= '9')
        {
            return c - '0';
        }
        else if (c >= 'a' && c <= 'z')
        {
            return c - 'a' + 10;
        }
        else
        {
            throw new IllegalArgumentException("invalid char: " + c);
        }
    }
    
    public static String getWifiAddressBase64(Context context) {
        byte[] address = getWifiAddressBytes(context);
        
        return Base64.encodeToString(address, Base64.DEFAULT);
    }
    
    public static byte[] getWifiHostAddressBytes(Context context) {
        Slog.d("getWifiHostAddressBytes E");
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifiManager.getDhcpInfo();

        Slog.d("dhcpinfo = " + dhcp);
        
        int host = (dhcp.ipAddress & ~dhcp.netmask);
        
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++) {
            quads[k] = (byte) ((host >> k * 8) & 0xFF);
        }
        Slog.d("getWifiHostAddressBytes X");
        return quads;
    }
    
    public static String getWifiHostAddressBase64(Context context) {
        Slog.d("getWifiHostAddressBase64 E");
        byte[] address = getWifiHostAddressBytes(context);
        
        Slog.d("wifi host address bytes = " + Arrays.toString(address));
        
        // skip subnet masked bytes
        List<Byte> input = new ArrayList<Byte>();
        for (byte segment : address) {
            if (segment != 0) {
                input.add(segment);
            }
        }
        
        byte[] inputBytes = new byte[input.size()];
        
        for (int i = 0; i < inputBytes.length; i++) {
            inputBytes[i] = input.get(i);
        }
        Slog.d("inputBytes: " + Arrays.toString(inputBytes));
        
        String base64 = Base64.encodeToString(inputBytes, Base64.DEFAULT).trim();
        Slog.d("getWifiHostAddressBase64 X, base64 = " + base64);
        return base64;
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
