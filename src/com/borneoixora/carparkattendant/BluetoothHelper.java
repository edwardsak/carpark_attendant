package com.borneoixora.carparkattendant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class BluetoothHelper {
	
	public static final String TAG = "BLUETOOTH_HELPER";
	
	public static String errorMessage = "No Error";
	private static BluetoothAdapter myBluetoothAdapter;
	private static BluetoothDevice myDevice;
	private static BluetoothSocket mySocket = null;
	private static OutputStream myOutStream = null;
	private static InputStream myInStream = null;
	
	public static boolean OpenPrinter(String bDAddr)
	{
    	if(bDAddr == "" || bDAddr == null)
    	{
    		errorMessage = "No BDAddress.";
    		return false;
    	}
    	myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	if(myBluetoothAdapter == null)
    	{
    		errorMessage = "BluetoothAdaper is null.";
    		return false;
    	}
    	myDevice = myBluetoothAdapter.getRemoteDevice(bDAddr);
    	if(myDevice == null)
    	{
    		errorMessage = "Device is null.";
    		return false;
    	}
    	
		if(!open(myBluetoothAdapter, myDevice))
		{
			return false;
		}
    	return true;
	}
	
	public static boolean open(BluetoothAdapter bluetoothAdapter, 
			BluetoothDevice btDevice)
	{
		boolean error=false;
		myBluetoothAdapter = bluetoothAdapter;
		myDevice = btDevice;

		if(!myBluetoothAdapter.isEnabled())
		{
			errorMessage = "BluetoothAdapter is disabled.";
	        return false;
		}
		myBluetoothAdapter.cancelDiscovery();

		try
		{
			//mySocket = myDevice.createRfcommSocketToServiceRecord(SPP_UUID);
			Method m = myDevice.getClass().getMethod("createRfcommSocket", new Class[] {int.class});
			mySocket = (BluetoothSocket) m.invoke(myDevice, 1); 
		}
		catch (SecurityException e){
			mySocket = null;
			errorMessage = "Security Exception.";
			return false;
		} 
		catch (NoSuchMethodException e) {
			mySocket = null;
			errorMessage = "No Such Method Exception.";
			return false;
		} catch (IllegalArgumentException e) {
			mySocket = null;
			errorMessage = "Illegal Argument Exception.";
			return false;
		} catch (IllegalAccessException e) {
			mySocket = null;
			errorMessage = "Illegal Access Exception.";
			return false;
		} catch (InvocationTargetException e) {
			mySocket = null;
			errorMessage = "Invocation Target Exception.";
			return false;
		}

		try 
		{
			mySocket.connect();
		} 
		catch (IOException e2) 
		{
			errorMessage = e2.getLocalizedMessage();
			mySocket = null;
			return false;
		}

		try 
		{
			myOutStream = mySocket.getOutputStream();
		} 
		catch (IOException e3) 
		{
			myOutStream = null;
			error = true;
		}

		try 
		{
			myInStream = mySocket.getInputStream();
		} 
		catch (IOException e3) 
		{
			myInStream = null;
			error = true;
		}

		if(error)
		{
			close();
			return false;
		}
		
		return true;
	}
	
	public static boolean close()
	{
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			Log.d(TAG, "Interrupted Exception.");
		}
		
		if(myOutStream != null)
		{
			try {
				myOutStream.flush();
			} catch (IOException e1) {
				Log.d(TAG, "IOException");
			}
			
			try {
				myOutStream.close();
			} catch (IOException e) {
				Log.d(TAG, "IOException");
			}
			myOutStream = null;
		}
		
		if(myInStream != null)
		{
			try {
				myInStream.close();
			} catch(IOException e) {
				Log.d(TAG, "IOException");
			}
			myInStream = null;
		}
		
		if(mySocket != null)
		{
			try {
				mySocket.close();
			} catch (IOException e) {
				Log.d(TAG, "IOException");
			}
			mySocket = null;
		}
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			Log.d(TAG, "Interrupted Exception.");
		}
		
		return true;
	}
	
	public static boolean write(byte[] data)
	{
		try 
		{
			myOutStream.write(data);
		} 
		catch (IOException e) 
		{
			errorMessage = "IOException";
			return false;
		}
		
		return true;
	}
	
	public static boolean write(byte[] data, int dataLen)
	{
		try 
		{
			myOutStream.write(data, 0, dataLen);
		} 
		catch (IOException e) 
		{
			errorMessage = "IOException";
			return false;
		}
		
		return true;
	}
	
	public static void flush()
	{
		int i = 0, dataLen = 0;
		try 
		{
			dataLen = myInStream.available();
		} 
		catch (IOException e1) 
		{
			Log.d(TAG, e1.getMessage());
		}
		for(i = 0; i < dataLen; i++)
		{
			try 
			{
				myInStream.read();
			} 
			catch (IOException e) 
			{
			}
		}
	}
	
	public static boolean read(byte[] data, int dataLen)
	{
		return readTimeout(data, dataLen, 2000);
	}
	
	public static boolean readTimeout(byte[] data, int dataLen, int timeout)
	{
		int i;
		for(i = 0; i < (timeout / 50); i++)
		{
			try 
			{
				if(myInStream.available() >= dataLen)
				{
					try 
					{
						myInStream.read(data, 0, dataLen);
						return true;
					} 
					catch (IOException e) 
					{
						errorMessage = "IOException";
						return false;
					}
				}
			} 
			catch (IOException e) 
			{
				errorMessage = "IOException";
				return false;
			}
			
			try 
			{
				Thread.sleep(50);
			} 
			catch (InterruptedException e) 
			{
				errorMessage = "interruptedException";
				return false;
			}
		}
		
		errorMessage = "Error";
		return false;
	}
}
