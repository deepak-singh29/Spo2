package com.example.spo2;
// SPo2 also called as Glucometer
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Calendar;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.spo2.AppSettings;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

public class Spo2Start extends ActionBarActivity {

	//Reading and correcting the month-value
    final Calendar c = Calendar.getInstance();
    int month = c.get(Calendar.MONTH)+1;
    
	private static final String TAG = "Spo2Start";
	private BluetoothSocket btSocket;
	TextView tvOS;
	int skipPackets = 0,sGraphData,spo2;
	float time = 0;
	double startTime = 0;
	LineGraphSeries<DataPoint> series;
	GraphView graph;
	Activity a;
	Thread t;
	
	boolean isReading = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Spo2");
		a = this;
		setContentView(R.layout.spo2_start);
		tvOS = (TextView)findViewById(R.id.tvOS);
		btSocket = AppSettings.BtSocket;
		
				// TODO Auto-generated method stub
				createGraph();
				startTime = System.currentTimeMillis();
				isReading = true;
				getSpo2StreamData();		
		
	}
	
	
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		outState.putBoolean("IS_READING",isReading);
	}



	private void createGraph(){
		
			graph = (GraphView) findViewById(R.id.graph);
			series = new LineGraphSeries<DataPoint>(new DataPoint[] {
			          new DataPoint(-2, 1),
			          new DataPoint(-1, 5)
			          
			});
			graph.addSeries(series);
			graph.getViewport().setScalable(true);
			graph.getViewport().setScrollable(true);
			graph.getViewport().setXAxisBoundsManual(true);
			graph.getViewport().setYAxisBoundsManual(true);
			graph.getViewport().setMaxX(2.5);//earlier 3
			graph.getViewport().setMaxY(600);
			graph.setTitle("PPG SIGNAL");
			graph.getGridLabelRenderer().setHorizontalAxisTitle("Time(sec)");
			graph.getGridLabelRenderer().setVerticalAxisTitle("Amplitude");
			
			
				// TODO Auto-generated method stub
				series.setOnDataPointTapListener(new OnDataPointTapListener() {
				    @Override
				    public void onTap(Series series, DataPointInterface dataPoint) {
				        Toast.makeText(a, "Series1: On Data Point clicked: "+dataPoint, Toast.LENGTH_SHORT).show();
				    }
				});
			
		
				
	}
	
	public void getSpo2StreamData() {
		// TODO Auto-generated method stub
		Log.d(TAG,"Inside SPO2 Start");
		// Keep listening to the InputStream while connected
        new Thread(new Runnable() {
        	BufferedReader tmpBuff = null;
    		BufferedReader bufferedReader;
//    		BufferedWriter bufferedWriter;
			@Override
			
			public void run() {
				// TODO Auto-generated method stub
				t = Thread.currentThread();
				OutputStream tmpBtOutputStream  = null;
				try {
	             	 tmpBtOutputStream = btSocket.getOutputStream();
//	             	bufferedWriter = new BufferedWriter(new OutputStreamWriter(tmpBtOutputStream));
	             	InputStream tmpBtInputStream = btSocket.getInputStream();
	             	bufferedReader = new BufferedReader(new InputStreamReader(tmpBtInputStream));

	                
	                 Log.e(TAG, "bluetooth sockets success");
	             } catch (IOException e) {
	                 Log.e(TAG, "bluetooth sockets (connectedThread) not created", e);
	             }
				Log.e(TAG,"Writing S");
            	try {
					tmpBtOutputStream.write((int)'S');
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
            	isReading = true;
	             
				while (isReading) {
		            try {
		            	
		            	
		            	//bufferedWriter.write('B');
		            	
		                // Read from the InputStream

//		                Log.e(TAG,"Hey");
		                String receivedData = bufferedReader.readLine().trim();
		                Log.e("spo2",receivedData);
		                if(receivedData.matches(AppSettings.SPORGEX)){
		                Log.e(TAG,receivedData);
		                
		                try {
		                	String [] temp = receivedData.split("[go]");
		                	
		                	try {
								sGraphData = Integer.parseInt(temp[1]);
								spo2 = Integer.parseInt(temp[2]);
								
								graph.post(new Runnable() {
								
									@Override
									public void run() {
										// TODO Auto-generated method stub
										time = (float) ((System.currentTimeMillis() - startTime)/1000);
										DataPoint d = new DataPoint(time, sGraphData);
										Log.e("sGraphData","X = "+d.getX()+"Y = "+d.getY());
										series.appendData(d, true, 500);
								        
									
									}
								});
							} catch (NumberFormatException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
								Log.e("Number format ERROR","Waiting for next data");
							}
		                	
		                	} catch (ArrayIndexOutOfBoundsException e) {
							// TODO: handle exception
							e.printStackTrace();
						}
		                
		                
//		   skipping 5 packets for convenient output             
//		             if(skipPackets > 5)
//		             {
//		                writing to text fields from thread
		                tvOS.post(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								tvOS.setText(Integer.toString(spo2));
							}
						});
		                               	                
		                skipPackets = 0;
		                
//		             }
//		                -------------------------
		              
//			           skipPackets++; 
		                }
		            } catch (IOException e) {
		                Log.e(TAG, "disconnected", e);
		               // Toast.makeText(getApplicationContext(), "Connection Lost", Toast.LENGTH_LONG).show();
		                break;
		            }
		        }
			}
		},"Spo2 Reading Thread").start();
	}
	//	code for Clickable Options textView
			public void txtClick_option(View v)
			{
				startActivity(new Intent(getApplicationContext(),Options.class));
			}
	// code for Clickable Help textview for Spo2
			public void textClick_Spo2Help(View view)
			{
				Log.d(TAG, "On click text Help");
				try {
					Intent spo2helpIntent = new Intent(Spo2Start.this, Help.class);
					spo2helpIntent.putExtra("HelpResolver", 's');
					startActivity(spo2helpIntent);
				} catch (Exception e) {
					e.printStackTrace();
					Log.e(TAG,"Exception while loading Help Class/Activity");
				}
				
				
			}
			@Override
		    public boolean onCreateOptionsMenu(Menu menu)
			{
				MenuInflater inflater = getMenuInflater();
				inflater.inflate(R.menu.spo2_start, menu);
				return true;
			} 
		    
		    		    
		    @Override
			public boolean onOptionsItemSelected(MenuItem item) {
				
				switch (item.getItemId()) {
					case R.id.ScreenShot:
					Bitmap bitmap = takeScreenshot();
				    saveBitmap(bitmap);
				    Toast.makeText(getApplicationContext(),"Screenshot saved in"+"   \""+AppSettings.directory+"\"  ", Toast.LENGTH_LONG).show();
				    break;
				    
					case R.id.Email:
						Intent mailIntent = new Intent(getApplicationContext(),Mail.class);
						startActivity(mailIntent);
						Toast.makeText(getApplicationContext(), "please fill details to send mail", Toast.LENGTH_SHORT).show();
						break;
				default:
					break;
				}
				
				
				return super.onOptionsItemSelected(item);
				
			}
		    
//		    methods for screenshot
		    public Bitmap takeScreenshot() {
				   View rootView = findViewById(android.R.id.content).getRootView();
				   rootView.setDrawingCacheEnabled(true);
				   return rootView.getDrawingCache();
				}
		//  methods for screenshot
			public void saveBitmap(Bitmap bitmap) {
				        
		        File imgDirectory = new File(AppSettings.directory);
		    	imgDirectory.mkdirs();
		        AppSettings.lastImage = AppSettings.directory + "Spo2_screenshot_" + c.get(Calendar.DAY_OF_MONTH)+"-"+month+"-"+c.get(Calendar.YEAR) + "_" + c.get(Calendar.HOUR_OF_DAY)+"-"+c.get(Calendar.MINUTE) + "-" + c.get(Calendar.SECOND) + ".png";
				
			    File imagePath = new File(AppSettings.lastImage);
			    FileOutputStream fos;
			    try {
			        fos = new FileOutputStream(imagePath);
			        bitmap.compress(CompressFormat.JPEG, 100, fos);
			        fos.flush();
			        fos.close();
			    } catch (FileNotFoundException e) {
			        Log.e("GREC", e.getMessage(), e);
			    } catch (IOException e) {
			        Log.e("GREC", e.getMessage(), e);
			    }
			    
			    
			   
			}
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				isReading = false;
				OutputStream tmpBtOutputStream  = null;
				try {
	             	 tmpBtOutputStream = btSocket.getOutputStream();           	               
	                 Log.e(TAG, "bluetooth sockets success");
	             } catch (IOException e) {
	                 Log.e(TAG, "bluetooth sockets (connectedThread) not created", e);
	             }
				Log.e(TAG,"Writing H");
            	try {
					tmpBtOutputStream.write((int)'H');
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				// TODO Auto-generated method stub
				
			}
			
		},"Thread writing H ").start();
		//finish();
	}
	
}
