package com.example.spo2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

public class Options extends Activity {

	public void onCreate(Bundle icicle)
	   {
	      super.onCreate(icicle);
	      requestWindowFeature(Window.FEATURE_NO_TITLE);
	      setContentView(R.layout.options);
	      
	      Button l = (Button)findViewById(R.id.bt_language);
	        l.setOnClickListener(new View.OnClickListener() {
	           public void onClick(View arg0) {
	           Intent i = new Intent(Options.this, Language.class);
	           startActivity(i);
	           }
	        });
	        
	        ToggleButton tb = (ToggleButton)findViewById(R.id.toggleButtonAutoconnect);
	        tb.setChecked(AppSettings.autoReconnect);
	   }
	
	public void onAutoconnectToogleClicked(View view) {
		
	    // Is the toggle on?
	    boolean on = ((ToggleButton) view).isChecked();
	    
	    if (on) {
	        AppSettings.autoReconnect = true;
	    } else {
	        AppSettings.autoReconnect = false;
	    }
	    
	    // Editor object to make preference changes.
        SharedPreferences autoconnectSettings = getSharedPreferences(AppSettings.PREFERENCES_BT_AUTORECONNECT, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = autoconnectSettings.edit();
        editor.putBoolean(AppSettings.AUTORECONNECT_KEY, AppSettings.autoReconnect);
        // Commit the edits
        editor.commit();
	}
	
	public void profiles(View v){
		Toast.makeText(getApplicationContext(), "Coming soon.... :)", Toast.LENGTH_LONG).show();
	}
}
