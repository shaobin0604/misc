
package com.example.testicsmenudot;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.WindowManager;

public class MainActivity extends Activity {

    private static final int FLAG_NEEDS_MENU_KEY = 0x08000000;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        WindowManager.LayoutParams attrs = getWindow().getAttributes();
        attrs.flags &= ~FLAG_NEEDS_MENU_KEY;
        getWindow().setAttributes(attrs); 
        
        setContentView(R.layout.activity_main);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.activity_main, menu);
//        return true;
//    }

}
