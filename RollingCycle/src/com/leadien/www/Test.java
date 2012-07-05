package com.leadien.www;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.leadien.www.widget.RollingCycle;

public class Test extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		RollingCycle fw = (RollingCycle) findViewById(R.id.rc);
		fw.setOnClickListener(l);
	}

	final View.OnClickListener l = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			System.out.println("jklhklhjkhkj");
		}
	};

}