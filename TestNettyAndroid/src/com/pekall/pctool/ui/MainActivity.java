
package com.pekall.pctool.ui;

import static com.pekall.pctool.ServerController.ACTION_SERVER_STATE_CHANGED;
import static com.pekall.pctool.ServerController.EXTRAS_STATE_KEY;
import static com.pekall.pctool.ServerController.STATE_CONNECTED;
import static com.pekall.pctool.ServerController.STATE_DISCONNECTED;
import static com.pekall.pctool.ServerController.STATE_START;
import static com.pekall.pctool.ServerController.STATE_STOP;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.pekall.pctool.R;
import com.pekall.pctool.ServerController;
import com.pekall.pctool.util.Slog;
import com.pekall.pctool.util.WifiUtil;

public class MainActivity extends Activity implements OnClickListener {
    private static final int FRAME_USB = 0;
    private static final int FRAME_WIFI = 1;
    
    private ViewFlipper mViewFlipper;
    
    // usb mode
    private TextView mTvUsbStatus;
    private ImageView mTbUsbStatus;
    private boolean mIsUsbOn;
    
    // wifi mode
    private TextView mTvWifiSecret;
    private TextView mTvWifiStatus;
    private ImageView mTbWifiStatus;
    private boolean mIsWifiOn;
    
    private boolean mDisplayUsbMode; 
    
    private BroadcastReceiver mServerStateReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Slog.d("action: " + action);
            if (ACTION_SERVER_STATE_CHANGED.equals(action)) {
                Bundle extras = intent.getExtras();
                int state = extras.getInt(EXTRAS_STATE_KEY);
                switch (state) {
                    case STATE_START: {
                        if (ServerController.isUsbMode()) {
                        	setUsbModeState(true);
                            mTvUsbStatus.setText(R.string.text_usb_in_service);
                            mViewFlipper.setDisplayedChild(FRAME_USB);
                        } else {
                        	setWifiModeState(true);
                            mTvWifiStatus.setText(R.string.text_wifi_in_service);
                            mTvWifiSecret.setText(getString(R.string.text_password, ServerController.getWifiSecret()));
                            mTvWifiSecret.setVisibility(View.VISIBLE);
                            mViewFlipper.setDisplayedChild(FRAME_WIFI);
                        }
                        break;
                    }
                    case STATE_STOP: {
                    	mTvWifiStatus.setText(R.string.text_wifi_tips);
                        mTvWifiSecret.setVisibility(View.INVISIBLE);
                        setWifiModeState(false);
                        mViewFlipper.setDisplayedChild(FRAME_WIFI);
                        break;
                    }
                    case STATE_CONNECTED: {
                        if (ServerController.isUsbMode()) {
                        	setUsbModeState(true);
                            mTvUsbStatus.setText(getString(R.string.text_usb_connected, ServerController.getHostname()));
                            mViewFlipper.setDisplayedChild(FRAME_USB);
                        } else {
                        	setWifiModeState(true);
                            mTvWifiStatus.setText(getString(R.string.text_wifi_connected, ServerController.getHostname()));
                            mTvWifiSecret.setText(getString(R.string.text_password, ServerController.getWifiSecret()));
                            mTvWifiSecret.setVisibility(View.VISIBLE);
                            mViewFlipper.setDisplayedChild(FRAME_WIFI);
                        }
                        break;
                    }
                    case STATE_DISCONNECTED: {
                        if (ServerController.isUsbMode()) {
                        	setUsbModeState(true);
                            mTvUsbStatus.setText(R.string.text_usb_in_service);
                            mViewFlipper.setDisplayedChild(FRAME_USB);
                        } else {
                        	setWifiModeState(true);
                            mTvWifiStatus.setText(R.string.text_wifi_in_service);
                            mViewFlipper.setDisplayedChild(FRAME_WIFI);
                        }
                        break;
                    }
                    default:
                        break;
                }
            }
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mViewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        
        mTvUsbStatus = (TextView) findViewById(R.id.tv_usb_status);
        mTbUsbStatus = (ImageView) findViewById(R.id.tb_usb_status);
        
        mTbUsbStatus.setOnClickListener(this);
        
        mTvWifiSecret = (TextView) findViewById(R.id.tv_wifi_secret);
        mTvWifiStatus = (TextView) findViewById(R.id.tv_wifi_status);
        mTbWifiStatus = (ImageView) findViewById(R.id.tb_wifi_status);
        
        mTbWifiStatus.setOnClickListener(this);
    }
    
    private void setWifiModeState(boolean on) {
        mIsWifiOn = on;
        if (on) {
            mTbWifiStatus.setImageResource(R.drawable.btn_close_wifi);
        } else {
            mTbWifiStatus.setImageResource(R.drawable.btn_start_wifi);
        }
    }
    
    private void setUsbModeState(boolean on) {
        mIsUsbOn = on;
        if (on) {
            mTbUsbStatus.setImageResource(R.drawable.btn_close_usb);
        } 
    }
    
    @Override
    protected void onStart() {
        super.onStart();
        Slog.d("onStart E, server state: " + ServerController.getServerStateDisplayString());
        
        mDisplayUsbMode = false;
        
        switch (ServerController.getServerState()) {
            case STATE_START: {
                mDisplayUsbMode = ServerController.isUsbMode();
                if (mDisplayUsbMode) {
                    setUsbModeState(true);
                    mTvUsbStatus.setText(R.string.text_usb_in_service);
                } else {
                    setWifiModeState(true);
                    mTvWifiStatus.setText(R.string.text_wifi_in_service);
                    mTvWifiSecret.setText(getString(R.string.text_password, ServerController.getWifiSecret()));
                    mTvWifiSecret.setVisibility(View.VISIBLE);
                }
                break;
            }
            case STATE_CONNECTED: {
                mDisplayUsbMode = ServerController.isUsbMode();
                if (mDisplayUsbMode) {
                    setUsbModeState(true);
                    mTvUsbStatus.setText(getString(R.string.text_usb_connected, ServerController.getHostname()));
                } else {
                    setWifiModeState(true);
                    mTvWifiStatus.setText(getString(R.string.text_wifi_connected, ServerController.getHostname()));
                    mTvWifiSecret.setText(getString(R.string.text_password, ServerController.getWifiSecret()));
                    mTvWifiSecret.setVisibility(View.VISIBLE);
                }
                break;
            }
            case STATE_DISCONNECTED: {
                if (ServerController.isUsbMode()) {
                    setUsbModeState(true);
                    mTvUsbStatus.setText(R.string.text_usb_in_service);
                } else {
                    setWifiModeState(true);
                    mTvWifiStatus.setText(R.string.text_wifi_in_service);
                }
                break;
            }
            case STATE_STOP: {
                 mTvWifiStatus.setText(R.string.text_wifi_tips);
                 mTvWifiSecret.setVisibility(View.INVISIBLE);
                 setWifiModeState(false);
                break;
            }
            default:
                break;
        }
        
        mViewFlipper.setDisplayedChild(mDisplayUsbMode ? FRAME_USB : FRAME_WIFI);
        
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SERVER_STATE_CHANGED);
        
        registerReceiver(mServerStateReceiver, intentFilter);
        
        Slog.d("onStart X");
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        Slog.d("onStop E");
        unregisterReceiver(mServerStateReceiver);
        Slog.d("onStop X");
    }

    @Override
    public void onClick(View v) {
        final Context context = getApplicationContext();
        if (v == mTbWifiStatus) {
            if (!mIsWifiOn) {
                
                // to make sure wifi is connected
                if (!WifiUtil.isWifiConnected(this)) {
                    showDialog();
                    return;
                }
                
//                String wifiSecret = WifiUtil.getWifiHostAddressBase64(context);
                
                String wifiSecret = WifiUtil.getWifiAddressRadix36Encoded(context);
                
                ServerController.setWifiSecret(wifiSecret);
                
                ServerController.startHttpService(context, /* usbMode */ false);
                ServerController.startFTPService(context);
            } else {
                ServerController.stopFTPService(context);
                ServerController.stopHttpService(context);
            }
        } else if (v == mTbUsbStatus) {
            if (mIsUsbOn) {
                ServerController.stopFTPService(context);
                ServerController.stopHttpService(context);
            }
        }
    }
    
    public void showDialog() {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance();
        newFragment.show(getFragmentManager(), "dialog");
    }
    
    public void doPositiveClick() {
        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
    }
    
    public void doNegativeClick() {
        
    }
    
    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance() {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("title", R.string.dlg_title_wifi_tips);
            args.putInt("text", R.string.dlg_text_wifi_tips);
            frag.setArguments(args);
            return frag;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Bundle arguments = getArguments();
            int title = arguments.getInt("title");
            int text = arguments.getInt("text");
            return new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(text)
                    .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((MainActivity)getActivity()).doPositiveClick();
                            }
                        }
                    )
                    .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                ((MainActivity)getActivity()).doNegativeClick();
                            }
                        }
                    )
                    .create();
        }
    }
}
