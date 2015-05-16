/****************************************************************************************************************																						*
 * Description: establish Bluetooth connection and receive data													*
 * 				this class is based on the BluetoothChatService from the Android Developer Site					*
 ****************************************************************************************************************/

package com.example.spo2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import android.widget.Toast;


public class BluetoothConnection {

	// tag for debugging information
	private static final String TAG = "BluetoothConnection";
	private static final boolean D = false;
	
	// Unique UUID for this application
    private static final UUID MY_UUID =
    		UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_CONNECTING = 1; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 2;  // now connected to a remote device
    
 // Message types sent from the Bluetooth Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_DEVICE_INFO = 3;	//
    public static final int MESSAGE_TOAST = 4;
    
    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String TOAST = "toast";
    
    private BluetoothAdapter btAdapter;
    private int connectionState;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private String connectedDeviceName = null;
    private String connectedDeviceAddress = null;
    
    private Context context;
    
    private final Handler btConnectionHandler;
    
    // Handler for bluetooth information
    private final Handler btHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            case MESSAGE_DEVICE_INFO:
	                // save the connected device's name
	                connectedDeviceName = msg.getData().getString(DEVICE_NAME);
	                connectedDeviceAddress = msg.getData().getString(DEVICE_ADDRESS);
	                
	                Toast.makeText(context, context.getString(R.string.connected_to) + " " + connectedDeviceName, Toast.LENGTH_SHORT).show();
	                break;
	            case MESSAGE_TOAST:
	                Toast.makeText(context, msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
	                break;
	            }
	        }
	    };

	  
    // Constructors
   public BluetoothConnection(Context context) {
        
    	btAdapter = BluetoothAdapter.getDefaultAdapter();
        this.context = context;
        btConnectionHandler = null;
        
        connectionState = STATE_NONE;
        
        Log.i(TAG, "Constructor");
    }	
    
   // the handler is for notifying about change of bluetooth connection status
    public BluetoothConnection(Context context, Handler connectionHandler) {
        
    	btAdapter = BluetoothAdapter.getDefaultAdapter();
        this.context = context;
        btConnectionHandler = connectionHandler;
        
        connectionState = STATE_NONE;
        
        Log.i(TAG, "BluetoothConnection");
    }	
	
    
    public BluetoothAdapter getBluetoothAdapter() {
    	return btAdapter;
    }
    
    public String getConnectedDeviceName() {
    	return connectedDeviceName;
    } 

    public String getConnectedDeviceAddress() {
    	return connectedDeviceAddress;
    }
    
     // Set the current state of the chat connection
     // state: defining the current connection state
    private synchronized void setState(int state) {
    	
    	Log.i(TAG, "setState");
    	
    	if (D) Log.d(TAG, "Bluetooth connection state: " + state);
        connectionState = state;
        
        // notify about change of bluetooth status 
        if (btConnectionHandler != null) {
	        switch(state) {
		    case BluetoothConnection.STATE_CONNECTED:
		    	btConnectionHandler.obtainMessage(STATE_CONNECTED).sendToTarget();
		        break;
		    case BluetoothConnection.STATE_CONNECTING:
		    	btConnectionHandler.obtainMessage(STATE_CONNECTING).sendToTarget();
		        break;
		    case BluetoothConnection.STATE_NONE:
		    	btConnectionHandler.obtainMessage(STATE_NONE).sendToTarget();
		        break;
		    }
        }
    }

    // Returns the current connection state.
    public synchronized int getState() {
    	Log.i(TAG, "getState");
        return connectionState;
    }
    
	public void connectToDevice(Intent data) {
		Log.i(TAG, "connectToDevice");
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = btAdapter.getRemoteDevice(address);
		// Attempt to connect to the device
        connect(device);
    }
    
	public void connectToDevice(String btDeviceAddress) {
		Log.i(TAG, "connectToDevice");
		// Get the BluetoothDevice object
		BluetoothDevice device  = btAdapter.getRemoteDevice(btDeviceAddress);
		// Attempt to connect to the device
        connect(device);
	}
	
     // Start the ConnectThread to initiate a connection to a remote device.
    public synchronized void connect(BluetoothDevice device) {
    	Log.i(TAG, "connect");
    	if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (connectionState == STATE_CONNECTING) {
            if (connectThread != null) {
            	connectThread.cancel(); 
            	connectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (connectedThread != null) {
        	connectedThread.cancel(); 
        	connectedThread = null;
        }

        // Start the thread to connect with the given device
        connectThread = new ConnectThread(device);
        connectThread.start();
        setState(STATE_CONNECTING);
    }
   
    // Start the ConnectedThread to begin managing a Bluetooth connection
   public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
	   Log.i(TAG, "connected");
	   if (D) Log.d(TAG, "connected");

       // Cancel the thread that completed the connection
       if (connectThread != null) {
    	   connectThread.cancel(); 
    	   connectThread = null;
       }

       // Cancel any thread currently running a connection
       if (connectedThread != null) {
    	   connectedThread.cancel(); 
    	   connectedThread = null;
       }

       // Start the thread to manage the connection and perform transmissions
       connectedThread = new ConnectedThread(socket);
       connectedThread.start();

       // Send the name and of the connected device
       Message msg = btHandler.obtainMessage(MESSAGE_DEVICE_INFO);
       Bundle bundle = new Bundle();
       bundle.putString(DEVICE_NAME, device.getName());
       bundle.putString(DEVICE_ADDRESS, device.getAddress());
       msg.setData(bundle);
       btHandler.sendMessage(msg);
       
       setState(STATE_CONNECTED);
   }

  // Indicate that the connection attempt failed and notify the UI Activity.
  private void connectionFailed() {
      // Send a failure message back to the Activity
	  Log.i(TAG, "connectionFailed");
	  Message msg = btHandler.obtainMessage(MESSAGE_TOAST);
      Bundle bundle = new Bundle();
      bundle.putString(TOAST, context.getString(R.string.unable_connect));
      msg.setData(bundle);
      btHandler.sendMessage(msg);
      
      setState(STATE_NONE);
  }
  
  // Indicate that the connection was lost
  private void connectionLost() {
	  Log.i(TAG, "connectionLost");
     
	  if (AppSettings.showConnectionLost)
	  {
		 // Send a failure message back to the Activity
	     Message msg = btHandler.obtainMessage(MESSAGE_TOAST);
	     Bundle bundle = new Bundle();
	     bundle.putString(TOAST, context.getString(R.string.connection_lost));
	     msg.setData(bundle);
	     btHandler.sendMessage(msg);
	  }
  }
   
	  // Stop all threads
	 public synchronized void stop() {
		 if (D) Log.d(TAG, "stop");
	
	     if (connectThread != null) {
	         connectThread.cancel();
	         connectThread = null;
	     }
	
	     if (connectedThread != null) {
	         connectedThread.cancel();
	         connectedThread = null;
	     }
	
	     setState(STATE_NONE);
	 }
  

  
  
  
   /**********************************************************************************************************
    * This thread runs while attempting to make an outgoing connection
    * with a device. It runs straight through; the connection either
    * succeeds or fails.
    **********************************************************************************************************/
   private class ConnectThread extends Thread {
	   
       private final BluetoothSocket btSocket;
       private final BluetoothDevice btDevice;

       public ConnectThread(BluetoothDevice device) {
    	   
    	   BluetoothSocket tmpBtSocket;
    	   btDevice = device;

           // Get a BluetoothSocket for a connection with the given BluetoothDevice
           try {
        	   tmpBtSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
               
           } catch (IOException e) {
               Log.e(TAG, "creating bluetooth socket failed", e);
               tmpBtSocket = null;
           }
           btSocket = tmpBtSocket;
           AppSettings.BtSocket = tmpBtSocket;
       }

       public void run() {
           
           setName("ConnectThread");

           // Always cancel discovery because it will slow down a connection
           btAdapter.cancelDiscovery();

           // Make a connection to the BluetoothSocket
           try {
               // This is a blocking call and will only return on a successful connection or an exception
               btSocket.connect();
           } catch (IOException e) {
               // Close the socket
               try {
                   btSocket.close();
               } catch (IOException e2) {
                   Log.e(TAG, "unable to close socket during connection failure", e2);
               }
               connectionFailed();
               return;
           }

           // Reset the ConnectThread because we're done
           synchronized (BluetoothConnection.this) {
               connectThread = null;
           }

           // Start the connected thread
           connected(btSocket, btDevice);
       }

       public void cancel() {
           try {
               btSocket.close();
           } catch (IOException e) {
               Log.e(TAG, "closing socket failed", e);
           }
       }
   }
   
   
   
   
   /*****************************************************************************************************
    * This thread runs during a connection with a remote device.
    * It handles all incoming transmissions.
    *****************************************************************************************************/
   private class ConnectedThread extends Thread {
	   
       private final BluetoothSocket btSocket;
       private final BufferedReader bufferedReader;

       public ConnectedThread(BluetoothSocket socket) {
    	   if (D) Log.d(TAG, "create ConnectedThread: ");
    	   InputStream tmpBtInputStream = null;
    	   BufferedReader tmpBuff = null;
           btSocket = socket;
           setName("Connected Thread");
           // Get the BluetoothSocket input streams
           try {
               tmpBtInputStream = socket.getInputStream();
               tmpBuff = new BufferedReader(new InputStreamReader(tmpBtInputStream));
           } catch (IOException e) {
               Log.e(TAG, "bluetooth sockets (connectedThread) not created", e);
           }
           bufferedReader = tmpBuff;
       }

       public void run() {
    	   
    	   if (D) Log.i(TAG, "BEGIN connectedThread");

           // Keep listening to the InputStream while connected
           /*while (true) {
               try {
                   // Read from the InputStream
                   
                   String receivedData = bufferedReader.readLine();
                   
                   Log.d(TAG,receivedData);
                  
                   
                   if(AppSettings.dataFacade!=null)
                	   AppSettings.dataFacade.splitData(receivedData);	                
	                
               } catch (IOException e) {
                   Log.e(TAG, "disconnected", e);
                   connectionLost();
                   break;
               }
           }*/
       }

       public void cancel() {
           try {
               btSocket.close();
           } catch (IOException e) {
               Log.e(TAG, "close() of connect socket failed", e);
           }
       }
   }

}
