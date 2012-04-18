package cn.yo2.aquarium.example.testcall;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TestCallActivity extends Activity implements OnClickListener {
	
	private Button mCall;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mCall = (Button) findViewById(R.id.call);
        
        mCall.setOnClickListener(this);
    }

	@Override
	public void onClick(View v) {
		if (v == mCall) {
			final Uri callUri = Uri.fromParts("tel", "10010", null);
            Intent intent = new Intent(Intent.ACTION_CALL, callUri);
            
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            
            startActivity(intent);
		}
	}
}