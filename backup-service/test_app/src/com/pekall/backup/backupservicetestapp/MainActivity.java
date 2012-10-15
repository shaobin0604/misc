package com.pekall.backup.backupservicetestapp;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.pekall.backup.Backup;

import java.util.Arrays;

public class MainActivity extends Activity implements OnClickListener {
    private static final String PACKAGE_NAME = "com.rovio.angrybirds";
    private Button mBtnDoBackup;
    private Button mBtnDoRestore;
    private TextView mTvPackageName;
    
    private Backup mBackup;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mBtnDoBackup = (Button) findViewById(R.id.do_backup);
        mBtnDoBackup.setOnClickListener(this);
        
        mBtnDoRestore = (Button) findViewById(R.id.do_restore);
        mBtnDoRestore.setOnClickListener(this);
        
        mTvPackageName = (TextView) findViewById(R.id.package_name);
        mTvPackageName.setText(PACKAGE_NAME);
        
        mBackup = new Backup(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnDoBackup) {
            mBackup.backupData(getExternalFilesDir(null).getAbsolutePath(), PACKAGE_NAME);
        } else if (v == mBtnDoRestore) {
            mBackup.restoreData(getExternalFilesDir(null).getAbsolutePath(), PACKAGE_NAME);
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        mBackup.release();
        mBackup = null;
    }
}
