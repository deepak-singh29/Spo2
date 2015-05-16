package com.example.spo2;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;


public class Credits extends Activity {
	
	   public void onCreate(Bundle icicle)
	   {
	      super.onCreate(icicle);
	      requestWindowFeature(Window.FEATURE_NO_TITLE);
	      setContentView(R.layout.credits);
	      TextView txt = (TextView) findViewById(R.id.credits);
	      Typeface font = Typeface.createFromAsset(getAssets(), "Chantelli_Antiqua.ttf");
	      txt.setTypeface(font);
	      
	   }
	}

