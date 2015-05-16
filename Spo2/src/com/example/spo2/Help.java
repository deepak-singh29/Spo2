package com.example.spo2;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class Help extends Activity {

	private static final String TAG = "Help Resolver";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Log.d(TAG, "Inside Help Class");
		Intent i = getIntent();
		char c = i.getCharExtra("HelpResolver",'o');
		switch(c)
		{
		
		case 's':
			Log.d(TAG, "Inside Help Spo2 ");
			setContentView(R.layout.help_spo2);
			break;
		default :
			Toast.makeText(getApplicationContext(), "Sorry nothing to help you ", Toast.LENGTH_SHORT).show();
			
		
		}
		
		
	}

}
