package com.pekall.smartplug.smartplugapp;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class Lamp extends View {
    
    private boolean mState; // true for on, false for off
    
    public Lamp(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
        setState(false);
    }

    public Lamp(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        setState(false);
    }

    public Lamp(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
        setState(false);
    }
    
    /**
     * Turn lamp on/off
     * 
     * @param state true for on, false for off
     */
    public void setState(boolean state) {
        mState = state;
        setBackgroundColor(state ? Color.WHITE : Color.BLACK);
    }
    
    /**
     * Get current state, true for on, false for off
     * 
     * @return the lamp's current on/off state
     */
    public boolean getState() {
        return mState;
    }
}
