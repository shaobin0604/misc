
package com.pekall.pctool;

import android.app.Service;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.IBinder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class WifiBroadcastService extends Service {

    private static final int DISCOVERY_PORT = 2562;
    private static final int TIMEOUT_MS = 500;
    private static final int BROADCAST_INTERVAL_MS = 1000;
    private volatile boolean mIsBroadcastRunning = false;
    private WifiManager mWifiManager;
    
    private Thread mDiscoverThread;

    @Override
    public void onCreate() {
        super.onCreate();
        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!mIsBroadcastRunning) {
            mIsBroadcastRunning = true;
            mDiscoverThread = new DiscoverThread();
            mDiscoverThread.start();
        }
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mIsBroadcastRunning) {
            mIsBroadcastRunning = false;
            try {
                if (mDiscoverThread != null) { 
                    mDiscoverThread.join();
                }
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }
    
    private class DiscoverThread extends Thread {
        /**
         * Calculate the broadcast IP we need to send the packet along. If we send
         * it to 255.255.255.255, it never gets sent. I guess this has something to
         * do with the mobile network not wanting to do broadcast.
         */
        private InetAddress getBroadcastAddress() throws IOException {
            DhcpInfo dhcp = mWifiManager.getDhcpInfo();
            if (dhcp == null) {
                Slog.d("Could not get dhcp info");
                return null;
            }

            int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
            byte[] quads = new byte[4];
            for (int k = 0; k < 4; k++)
                quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
            return InetAddress.getByAddress(quads);
        }

        @Override
        public void run() {
            Slog.d("Discover thread E");
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket(DISCOVERY_PORT);
                socket.setBroadcast(true);
                socket.setSoTimeout(TIMEOUT_MS);
                
                StringBuilder builder = new StringBuilder("pekallpcsuite|");
                builder.append(Build.MODEL);
                
                byte[] bytes = builder.toString().getBytes();
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length,
                        getBroadcastAddress(), DISCOVERY_PORT);
                
                while (mIsBroadcastRunning) {
                    socket.send(packet);
                    
                    Thread.sleep(BROADCAST_INTERVAL_MS);
                }
            } catch (SocketException e) {
                Slog.e("Error send packet", e);
            } catch (IOException e) {
                Slog.e("Error send packet", e);
            } catch (InterruptedException e) {
                Slog.e("Error send packet", e);
            } finally {
                if (socket != null) {
                    socket.close();
                }
                Slog.d("Discover thread X");
            }
        }
    }
}
