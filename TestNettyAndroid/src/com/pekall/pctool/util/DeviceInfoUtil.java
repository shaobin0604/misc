package com.pekall.pctool.util;

import android.content.Context;
import android.telephony.TelephonyManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.UUID;

public class DeviceInfoUtil {
    private static String sID = null;
    private static final String INSTALLATION = "INSTALLATION";
    
    public static String getDeviceModel() {
        return android.os.Build.MODEL;
    }

    public synchronized static String getDeviceUuid(Context context) {
        if (sID == null) {  
            File installation = new File(context.getFilesDir(), INSTALLATION);
            try {
                if (!installation.exists())
                    writeInstallationFile(installation);
                sID = readInstallationFile(installation);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return sID;
    }

    private static String readInstallationFile(File installation) throws IOException {
        RandomAccessFile f = new RandomAccessFile(installation, "r");
        byte[] bytes = new byte[(int) f.length()];
        f.readFully(bytes);
        f.close();
        return new String(bytes);
    }

    private static void writeInstallationFile(File installation) throws IOException {
        FileOutputStream out = new FileOutputStream(installation);
        String id = UUID.randomUUID().toString();
        out.write(id.getBytes());
        out.close();
    }
    
    public static String getBuildSerial() {
        return android.os.Build.SERIAL;
    }
    
    /**
     * Get phone's IMEI
     * 
     * <b>NOTE: </b> IMEI is only available on device with radio access
     * @param context
     * @return
     */
    public static String getImei(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }
}
