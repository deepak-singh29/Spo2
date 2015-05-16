 package com.example.spo2;
/****************************************************************************************************************
 * Author: 																					*
 * Date: 																							*
 * Description: Main Activity for connecting to a bluetooth device												*
 ****************************************************************************************************************/


import com.example.spo2.Spo2Start;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

public class MainScreen extends Activity {

	private static final String TAG = "MainScreen";
	private static final Boolean D = true;
	
	// Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    
    // Constants that indicate the current connection state
    private static final int STATE_NONE = 0;       // we're doing nothing
    private static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    private static final int STATE_CONNECTED = 2;  // now connected to a remote device
    
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.mainscreen);
		
		Button c = (Button)findViewById(R.id.buttonCredits);
        c.setOnClickListener(new View.OnClickListener() {
           public void onClick(View arg0) {
           Intent i = new Intent(MainScreen.this, Credits.class);
           startActivity(i);
           }
        });
        
        Button o = (Button)findViewById(R.id.buttonOptions);
        o.setOnClickListener(new View.OnClickListener() {
           public void onClick(View arg0) {
           Intent i = new Intent(MainScreen.this, Options.class);
           startActivity(i);
           }
        });
        
        Button h = (Button)findViewById(R.id.buttonHelp);
        h.setOnClickListener(new View.OnClickListener() {
           public void onClick(View arg0) {
           Intent i = new Intent(MainScreen.this, Help.class);
//           to resolve help calls
           i.putExtra("HelpResolver", 's');
           startActivity(i);
           }
        });
		
		if (D) Log.e(TAG, "++ ON CREATE ++");
	}
	
	@Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "++ ON START ++");

    }
	
	@Override
    public void onDestroy() {
        super.onDestroy();
        
        if (D) Log.e(TAG, "--- ON DESTROY ---");
        
        // Stop the Bluetooth services
        if (AppSettings.bluetoothConnection != null) 
        	AppSettings.bluetoothConnection.stop();
    }
//	this method is not used here it has to send to interface after homesceen - ecg-startButton
//	public void buttonClick_visualize(View view)
	public void buttonClick_connect(View view)
	{
		AppSettings.showConnectionLost = false;
		Log.i(TAG, "showConnectionLost = false");
		
		AppSettings.bluetoothConnection = new BluetoothConnection(this.getApplicationContext(), this.btConnectionHandler);
		BluetoothAdapter btAdapter = AppSettings.bluetoothConnection.getBluetoothAdapter();
		
		// If the adapter is null, then Bluetooth is not supported
        if (btAdapter == null) {
        	Toast.makeText(getApplicationContext(), R.string.bt_not_available, Toast.LENGTH_LONG).show();
        }
        else {
        	// If Bluetooth is not on, request that it be enabled.
            if (!btAdapter.isEnabled()) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            }
            else {
            	// Launch the DeviceListActivity to see devices and do scan
               Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
               startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            }
        }
	}
	
	
	public static void disconnect() {
		
		// Stop the Bluetooth chat services
        if (AppSettings.bluetoothConnection != null) 
        	AppSettings.bluetoothConnection.stop();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
        switch (requestCode) {
        case REQUEST_ENABLE_BT:
        	// When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getApplicationContext(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
  	
            } else {
                // User did not enable Bluetooth or an error occurred
            	if (D) Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled, Toast.LENGTH_SHORT).show();
            }
            break;
        case REQUEST_CONNECT_DEVICE:
        	// When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
            	AppSettings.showConnectionLost = true;
            	AppSettings.bluetoothConnection.connectToDevice(data);
//            	starting spo2 activity
            	Intent spo2Activity = new Intent(getApplicationContext(), Spo2Start.class);
                startActivity(spo2Activity);
//            	this method is not used here it has to send to interface after homesceen - ecg-startButton
               /* if (D) Log.d(TAG, "start visualization activity");
        		Intent visualizeActivity = new Intent(getApplicationContext(), Visualization.class);
                startActivity(visualizeActivity);*/
            }
            break;
        }
    }

	//Handler for updating TextView of bluetooth connection state
	private final Handler btConnectionHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            case STATE_CONNECTED:
	            	if (D) Log.d(TAG, "connected");
	            	
	            	// Editor object to make preference changes.
	                SharedPreferences autoconnectSettings = getSharedPreferences(AppSettings.PREFERENCES_BT_AUTORECONNECT, Context.MODE_PRIVATE);
	                SharedPreferences.Editor editor = autoconnectSettings.edit();
	                editor.putBoolean(AppSettings.AUTORECONNECT_KEY, AppSettings.autoReconnect);
	                editor.putString(AppSettings.BT_AUTORECONNECT_ADDRESS_KEY, AppSettings.bluetoothConnection.getConnectedDeviceAddress());
	                Log.e(TAG, "address: " + AppSettings.bluetoothConnection.getConnectedDeviceAddress());
	                // Commit the edits
	                editor.commit();
	                
	                break;
	            case STATE_CONNECTING:
	            	if (D) Log.d(TAG, "connecting");
	            	break;
	            case STATE_NONE:
	            	if (D) Log.d(TAG, "not connected");
	            	break;
	            }
	        }
	};
	

}
