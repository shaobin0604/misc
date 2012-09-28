
package com.pekall.smartplug.example;

import com.pekall.smartplug.SmartPlug;
import com.pekall.smartplug.SmartPlug.SmartPlugListener;
import com.pekall.smartplug.SmartPlugImpl;

import java.util.Scanner;

public class Main {
    
    
    public static void main(String[] args) {
        final String host = "192.168.20.102";
        final int port = 16668;
        
        Plug plug = new Plug();
        SmartPlugListener listener = new SmartPlugListenerImpl(plug);
        SmartPlug smartPlug = new SmartPlugImpl(listener);
        
        boolean success = smartPlug.connect(host, port);
        if (!success) {
            System.out.println("connect fail!");
            return;
        }
        
        System.out.println("connect ok");
        
        final String sn = "1";
        final String pn = "2";
        
        
        success = smartPlug.login(pn, sn);
        
        System.out.println("login " + (success ? "ok" : "fail"));
        
        //
        // wait user input 
        //
        
        Scanner sc = new Scanner(System.in);
        int i = sc.nextInt();
        
        smartPlug.disconnect();
        smartPlug.release();
    }
    
    private static class SmartPlugListenerImpl implements SmartPlugListener {
        
        private Plug mPlug;

        public SmartPlugListenerImpl(Plug plug) {
            super();
            if (plug == null) {
                throw new IllegalArgumentException("plug should not be null");
            }
            this.mPlug = plug;
        }

        @Override
        public boolean onSetStatusRequested(SmartPlug smartPlug, boolean on) {
            return mPlug.setStatus(on);
        }

        @Override
        public boolean onGetStatusRequested(SmartPlug smartPlug) {
            return mPlug.getStatus();
        }

        @Override
        public void onError(SmartPlug smartPlug, String error) {
            // TODO: update ui
            System.out.println("Error: " + error);
        }

        @Override
        public void onDisconnected(SmartPlug smartPlug) {
            // TODO Auto-generated method stub
            System.out.println("Disconnected");
        }
        
    }
}
