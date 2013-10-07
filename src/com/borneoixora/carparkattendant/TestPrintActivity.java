package com.borneoixora.carparkattendant;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import zpSDK.zpSDK.zpSDK;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class TestPrintActivity extends Activity {
	
	private static final String TAG = "TEST_PRINT_ACTIVITY";

	public static BluetoothAdapter myBluetoothAdapter;
	public String selectedBDAddress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_print);
		
		//if(!listBluetoothDevice())
		//	finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test_print, menu);
		return true;
	}
	
	public boolean listBluetoothDevice()
    {
		try {
	        final List<Map<String,String>> list=new ArrayList<Map<String, String>>(); 
	        ListView listView = (ListView) findViewById(R.id.listView1);
	        SimpleAdapter m_adapter = new SimpleAdapter( this,list,
			   		android.R.layout.simple_list_item_2,
			   		new String[]{"DeviceName","BDAddress"},
			   		new int[]{android.R.id.text1,android.R.id.text2}
			   		);
	        listView.setAdapter(m_adapter);
	
	        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	        if(myBluetoothAdapter == null)
	        {
	     		Toast.makeText(this,"BluetoothAdapter is null.", Toast.LENGTH_LONG).show();
	     		return false;
	        }
	
	        if(!myBluetoothAdapter.isEnabled())
	        {
	            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);    
	            startActivityForResult(enableBtIntent, 2);
	        }
	
	        Set <BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();
	        if (pairedDevices.size() <= 0)return false;
	        for (BluetoothDevice device : pairedDevices)
	        {
	        	Map<String,String> map=new HashMap<String, String>();
	        	map.put("DeviceName", device.getName()); 
	        	map.put("BDAddress", device.getAddress());
	        	list.add(map);
	        }
	        listView.setOnItemClickListener(new ListView.OnItemClickListener() 
	        {
	        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
	        	{
	        		selectedBDAddress = list.get(position).get("BDAddress");
	        		if (((ListView)parent).getTag() != null){
	        			((View)((ListView)parent).getTag()).setBackgroundDrawable(null);
	        		}
	        		((ListView)parent).setTag(view);
	        		view.setBackgroundColor(Color.BLUE);
				}
	        });
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}
        
        return true;
    }
	
	public boolean OpenPrinter(String bdAddress)
    {
		try {
	    	if(bdAddress == "" || bdAddress == null)
	    	{
				Toast.makeText(this,"BDAddress is null.", Toast.LENGTH_LONG).show();
	    		return false;
	    	}
	    	
			BluetoothDevice myDevice;
	    	myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	    	if(myBluetoothAdapter == null)
	    	{
				Toast.makeText(this,"BluetoothAdapter is null.", Toast.LENGTH_LONG).show();
				return false;
	    	}
	    	
	    	myDevice = myBluetoothAdapter.getRemoteDevice(bdAddress);
	    	if(myDevice == null)
	    	{
				Toast.makeText(this,"Device is null.", Toast.LENGTH_LONG).show();
				return false;
	    	}
	    	
			if(zpSDK.zp_open(myBluetoothAdapter, myDevice) == false)
			{
				Toast.makeText(this, zpSDK.ErrorMessage, Toast.LENGTH_LONG).show();
				return false;
			}
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}
		
		return true;
    }
	
	private void printReceipt(String bdAddress) {
		boolean isPrinterOpened = false;
		boolean isPageCreated = false;
		
		try {
			if(!OpenPrinter(bdAddress))
			{
				return;
			}
			isPrinterOpened = true;
			
			// create print page
			// zp_page_create(double pageWidth, double pageHeight)
			if(!zpSDK.zp_page_create(80, 108+64))
			{
	     		Toast.makeText(this,"Create page fail.", Toast.LENGTH_LONG).show();
	     		return;
			}
			isPageCreated = true;
			
			zpSDK.TextPosWinStyle = false;
			zpSDK.zp_draw_text_ex(18.3, 3.4, "Borneo Ixora Co","Arial", 3.4, 0, false, true, false);
			zpSDK.zp_draw_line(0, 5, 80, 5, 2);
	
			// return true = error
			if(zpSDK.zp_printer_check_error())
		    {
				Toast.makeText(this,zpSDK.ErrorMessage, Toast.LENGTH_LONG).show();
		    }
			else
			{
				// zp_page_print(boolean isRotate)
				if(!zpSDK.zp_page_print(false))
				{
					Toast.makeText(this,zpSDK.ErrorMessage, Toast.LENGTH_LONG).show();
				}
				
				// zp_goto_mark_right(int maxFeedMM)
				zpSDK.zp_goto_mark_right(30);
			}
	
			if(zpSDK.zp_printer_check_error())
		    {
				Toast.makeText(this,zpSDK.ErrorMessage, Toast.LENGTH_LONG).show();
		    }
		} catch (Exception e) {
			Log.d("", e.getMessage());
		} finally {
			// after zp_create_page, must call zp_page_free
			if (isPageCreated)
				zpSDK.zp_page_free();
			
			// after zp_open, must call zp_close
			if (isPrinterOpened)
				zpSDK.zp_close();
		}
	}
	
	public void print(View view) {
		//printReceipt("00:00:05:DB:86:33");
		printReceiptEsc("00:00:05:DB:86:33");
	}
	
	private void printReceiptEsc(String bdAddress) {
		try {
			if(!BluetoothHelper.OpenPrinter(bdAddress))
			{
				Toast.makeText(this, BluetoothHelper.errorMessage, Toast.LENGTH_LONG).show();
				return;
			}
			
			Toast.makeText(this, "Bluetooth connected.", Toast.LENGTH_SHORT).show();
			
			ZicoxEscHelper.initialize();
			ZicoxEscHelper.setLineGap(0);
			ZicoxEscHelper.write("\n");
			ZicoxEscHelper.setAlign(ZicoxEscHelper.ALIGN_LEFT);
			ZicoxEscHelper.setFontSize(ZicoxEscHelper.FONT_SIZE_1XW_2XH);
			ZicoxEscHelper.write("Hello World!\n");
			ZicoxEscHelper.setAlign(ZicoxEscHelper.ALIGN_CENTER);
			ZicoxEscHelper.setFontSize(ZicoxEscHelper.FONT_SIZE_1XW_2XH);
			ZicoxEscHelper.write(String.format("%s\n", "2011-05-16"));
			ZicoxEscHelper.write("\n\n\n\n");
			
			Toast.makeText(this, "Print completed.", Toast.LENGTH_SHORT).show();
			
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
		}
		finally {
			BluetoothHelper.close();
		}
	}

}
