package com.example.spo2;

import android.bluetooth.BluetoothSocket;

import com.example.spo2.BluetoothConnection;

public class AppSettings {
	
	public static String directory = "mnt/sdcard/Spo2/";
	public static String lastImage ="";
//	regular expression
	public static final String SPORGEX ="g\\d{1,4}o\\d{1,3}";
	 // Bluetooth auto-reconnect
 	public static boolean autoReconnect = false;
	protected static boolean ECGisReading = false;
 	public static final String PREFERENCES_BT_AUTORECONNECT = null;
 	public final static String AUTORECONNECT_KEY = "AUTRECONNECT";
 	public final static String BT_AUTORECONNECT_ADDRESS_KEY = "AUTORECONNECT_ADDRESS";
    
	//InterProcessCommunication
		public static BluetoothConnection bluetoothConnection = null;
		public static BluetoothSocket BtSocket = null;
	    public static Boolean showConnectionLost = true;
}
