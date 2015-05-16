package com.example.spo2;

/****************************************************************************************************************																							*
 * Description: Opens Splashscreen and closes it after a few seconds											*
 ****************************************************************************************************************/

import com.example.spo2.Spo2Start;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.widget.Toast;
 
public class Spo2Splash extends Activity {
 
    private final int SPLASH_DISPLAY_LENGHT = 2000;
    private static final Boolean D = true;
    private final String TAG = "Multipara (Splash screen)";
    
	// Intent request codes
    private static final int REQUEST_ENABLE_BT = 2;
    
    // Bluetooth device address for auto-reconnect
    private String btDeviceAddress = null;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
            
        // Restore (auto-reconnect) preferences
        SharedPreferences reconnectSettings = getSharedPreferences(AppSettings.PREFERENCES_BT_AUTORECONNECT, Context.MODE_PRIVATE);
        AppSettings.autoReconnect = reconnectSettings.getBoolean(AppSettings.AUTORECONNECT_KEY, false);
    	if (D) Log.e(TAG, "Autoreconnect: " + AppSettings.autoReconnect);
    	btDeviceAddress = reconnectSettings.getString(AppSettings.BT_AUTORECONNECT_ADDRESS_KEY, "");


        if (AppSettings.autoReconnect && btDeviceAddress != "")
        {
        	
        	
        	btDeviceAddress = reconnectSettings.getString(AppSettings.BT_AUTORECONNECT_ADDRESS_KEY, "");
        	if (D) Log.e(TAG, btDeviceAddress);
        	
        	AppSettings.bluetoothConnection = new BluetoothConnection(this.getApplicationContext(), null);
    		BluetoothAdapter btAdapter = AppSettings.bluetoothConnection.getBluetoothAdapter();
    		
    		// If the adapter is null, then Bluetooth is not supported
            if (btAdapter == null) {
            	Toast.makeText(getApplicationContext(), R.string.bt_not_available, Toast.LENGTH_LONG).show();
            }
            else {
            	// If Bluetooth is not enabled, request that it be enabled.
                if (!btAdapter.isEnabled()) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                }
                else {
                	// connect to Spo2
                	AppSettings.bluetoothConnection.connectToDevice(btDeviceAddress);
                    
                    if (D) Log.d(TAG, "start visualization activity");
            		Intent spo2Activity = new Intent(getApplicationContext(), Spo2Start.class);
                    startActivity(spo2Activity);
                    Spo2Splash.this.finish();
                }
            }
        }
        else
        {
        	/* New Handler to start the Menu-Activity
	         * and close this Splash-Screen after some seconds.*/
	        new Handler().postDelayed(new Runnable(){
	 
		        @Override
		        public void run() {
	 
		            /* Create an Intent that will start the Menu-Activity. */
		            Intent mainIntent = new Intent(Spo2Splash.this,MainScreen.class);
		            Spo2Splash.this.startActivity(mainIntent);
		            Spo2Splash.this.finish();
		        }
	        }, SPLASH_DISPLAY_LENGHT);
        }
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
        switch (requestCode) {
        case REQUEST_ENABLE_BT:
        	// When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // connect 
            	AppSettings.bluetoothConnection.connectToDevice(btDeviceAddress);
                
                if (D) Log.d(TAG, "start visualization activity");
                Intent spo2Activity = new Intent(getApplicationContext(), Spo2Start.class);
                startActivity(spo2Activity);
  	
            } else {
                // User did not enable Bluetooth or an error occurred
            	if (D) Log.d(TAG, "Bluetooth not enabled");
                Toast.makeText(this, R.string.bt_not_enabled, Toast.LENGTH_SHORT).show();
            }
            break;
        }
    }
}