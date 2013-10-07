package com.borneoixora.carparkattendant;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.wpx.bean.ResultInfo;
import com.wpx.myutil.PlaySoundUtil;
import com.wpx.service.IUHFService;
import com.wpx.service.impl.UHFServiceImpl;
import com.wpx.util.GlobalUtil;

public class ChargeActivity extends Activity {

	public static final String TAG = "ChargeActivity";
	
	private IUHFService uhfService = new UHFServiceImpl();
	
	public static BluetoothAdapter myBluetoothAdapter;
	
	private Handler _handler = new Handler();
	
	private ProgressDialog _progressDialog;
	
	private Button btnScan;
	private EditText txtLotNo;
	private EditText txtCarRegNo;
	
	private String _tranCode;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_charge);
		
		btnScan = (Button)findViewById(R.id.btnScan);
		btnScan.setEnabled(false);
		txtLotNo = (EditText)findViewById(R.id.txtLotNo);
		txtCarRegNo = (EditText)findViewById(R.id.txtCarRegNo);
		
		_tranCode = "";
		
		// set device type to cm390
		uhfService.setDeviceType(0);
		
		// set tag type to epc g2
		uhfService.setTagType(GlobalUtil.op.Tag_Type);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.charge, menu);
		return true;
	}
	
	@Override
	public void onResume() {
		new ConnectTask(this).execute();
		super.onResume();
	}
	
	@Override
	public void onPause() {
		new DisconnectTask().execute();
		super.onPause();
	}
	
	private void startProgress() {
		_progressDialog = new ProgressDialog(this);
		_progressDialog.setMessage("Please wait...");
		_progressDialog.setIndeterminate(true);
		_progressDialog.setCancelable(false);
		_progressDialog.show();
	}
	
	private void stopProgress() {
		if (_progressDialog.isShowing()) {
			_progressDialog.dismiss();
	    }
	}
	
	// --------------------------------------------------
	// connect
	class ConnectTask extends AsyncTask<Integer, String, Integer> {

		ResultInfo resultInfo = null;
		String version;
		
		Context _context;
		ProgressDialog _progressDialog;
		
		public ConnectTask(Context context) {
			_context = context;
		}
		
		//@Override
		protected void onPreExecute() {
			this._progressDialog = new ProgressDialog(_context);
			this._progressDialog.setMessage("Please wait...");
			this._progressDialog.setIndeterminate(true);
			this._progressDialog.setCancelable(false);
			this._progressDialog.show();
		    
			// connect uhf reader
			try {
				connect();
			} catch (Exception e) {
				String errorMessage = e.getMessage();
				Log.e(TAG, errorMessage);
				AlertRunnable.alert(_context, errorMessage);
			}
			
			super.onPreExecute();
		}
		
		private void connect() {
			int type = GlobalUtil.dev_type;
			uhfService.disConnected();
			if(type == 1)
				resultInfo = uhfService.connected(1);
			else{
				resultInfo = uhfService.connected(0);
			}
		}
		
		@Override
		protected Integer doInBackground(Integer... params) {
			if (resultInfo !=null) {
				if (resultInfo.getResult() == 0) {
					version = uhfService.getVersion();
					//uhfService.Channel_Calibration(4+1); //china
				}
			}
			return 0;
		}
		
		//@Override
		protected void onPostExecute(Integer result) {
			if (this._progressDialog.isShowing()) {
				this._progressDialog.dismiss();
		    }
			
			if(resultInfo !=null) {
				if(resultInfo.getResult() == 0) {
					Log.d(TAG, "Connected");
					
					Toast.makeText(getApplicationContext(), "Connected. Version=" + version,
							Toast.LENGTH_SHORT).show();
					
					btnScan.setEnabled(true);
				} else {
					String errorMessage = "Open port is failed : " + resultInfo.getResult();
					Log.e(TAG, errorMessage);
					
					AlertRunnable.alert(_context, errorMessage);
				}
			} else {
				// close
			}
		}
	}
	
	class DisconnectTask extends AsyncTask<Integer,String,Integer> {

		ResultInfo resultInfo = null;
		String version;
		
		//@Override
		protected void onPreExecute() {
			disconnect();
		}
		
		@Override
		protected Integer doInBackground(Integer... params) {
			return 0;
		}
		
		private void disconnect() {
			uhfService.disConnected();
		}
		
		//@Override
		protected void onPostExecute(Integer result) {
			Log.d(TAG, "Disconnected");
			
			//Toast.makeText(getApplicationContext(), "Disconnected",
			//		Toast.LENGTH_SHORT).show();
			
			btnScan.setEnabled(false);
		}
	}
	
	// --------------------------------------------------
	// Scan
	public void clickScan(View view) {
		startProgress();
		
		new Thread(new ScanTagRunnable()).start();
	}
		
	class ScanTagRunnable implements Runnable {

		ResultInfo _resultInfo;
		String _epcId;
		String _carRegNo;
		
		public ScanTagRunnable() {
			_resultInfo = null;
			_epcId = "";
			_carRegNo = "";
		}
		
		@Override
		public void run() {
			try {
				scan();
				
				// find car reg no.
				getCarRegNo(_epcId);
				
				// show car reg no.
				_handler.post(new ShowCarRegNoRunnable(_carRegNo));
				
			} catch (Exception e) {
				_handler.post(new AlertRunnable(ChargeActivity.this, e.getMessage()));
			} finally {
				_handler.post(new Runnable() {
					
					@Override
					public void run() {
						stopProgress();
					}
					
				});
			}
		}
		
		private void scan() throws Exception {
			_resultInfo = uhfService.getEPCList(uhfService.getQValue());
			
			if (_resultInfo!=null) {
				if (_resultInfo.getResult() == 0) {
					List<String> values = _resultInfo.getValues();
					if (values.size() > 0) {
						// tag found
						_epcId = values.get(0);
						Log.d(TAG, "Tag found:" + _epcId);
						
						// play sound
						_handler.post(new Runnable() {
							
							@Override
							public void run() {
								PlaySoundUtil.play();
							}
						});
					}
				} else {
					String errorMessage = "";
					
					switch (_resultInfo.getResult()) {
					case -23:
						errorMessage = "No Tag found.";
						break;
					default:
						errorMessage = "Scan tag failed. ErrorCode:" + _resultInfo.getResult();
						break;
					}
					
					//Log.e(TAG, errorMessage);
					throw new Exception(errorMessage);
				}
			}
		}
		
		private void getCarRegNo(String epcId) throws Exception {
			int epcId2 = Integer.parseInt(epcId);
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("tagId", "" + epcId2));
			
			FormPost formPost = new FormPost();
			String result = formPost.readJsonFeed(
					"http://10.5.1.14:8080/attendant/tag/find/", 
					nameValuePairs);
			
			JSONObject jsonObj;
			try {
				jsonObj = new JSONObject(result);
				
				boolean returnStatus = jsonObj.getBoolean("returnStatus");
				
				if (returnStatus) {
					_carRegNo = jsonObj.getString("carRegNo");
					Log.d(TAG, "Car found:" + _carRegNo);
					
				} else {
					String returnMessage = jsonObj.getString("returnMessage");
					//Log.e(TAG, returnMessage);
					throw new Exception(returnMessage);
				}
				
			} catch (JSONException e) {
				//String errorMessage = e.getMessage();
				//Log.e(TAG, errorMessage);
				throw e;
			}
		}
		
	}
	
	class ShowCarRegNoRunnable implements Runnable {

		private String _carRegNo; 
		
		public ShowCarRegNoRunnable(String carRegNo) {
			_carRegNo = carRegNo;
		}
		
		@Override
		public void run() {
			if (_progressDialog.isShowing()) {
				_progressDialog.dismiss();
		    }
			
			txtCarRegNo.setText(_carRegNo);
		}
		
	}
	
	/*class ScanTask extends AsyncTask<Integer,String,Integer> {
		
		ResultInfo resultInfo = null;
		ProgressDialog _progressDialog;
		
		//@Override
		protected void onPreExecute() {
			_progressDialog = new ProgressDialog(getApplicationContext());
			_progressDialog.setMessage("Please wait...");
			_progressDialog.setIndeterminate(true);
			_progressDialog.show();
			
			super.onPreExecute();
		}
				
		//@Override
		protected Integer doInBackground(Integer... params) {
			resultInfo = uhfService.getEPCList(uhfService.getQValue());
			return 0;
		}
		
		//@Override
		protected void onPostExecute(Integer result) {
			if (_progressDialog.isShowing()) {
				_progressDialog.dismiss();
		    }
			
			if (resultInfo!=null) {
				if (resultInfo.getResult() == 0) {
					List<String> values = resultInfo.getValues();
					if (values.size() > 0) {
						// tag found
						String epcId = values.get(0);
						Log.d(TAG, epcId);
						
						// find car reg no.
						
						PlaySoundUtil.play();
					}
				} else {
					String errorMessage = "";
					
					switch (resultInfo.getResult()) {
					case -23:
						errorMessage = "No Tag found.";
						break;
					default:
						errorMessage += "Scan tag failed. ErrorCode:" + resultInfo.getResult();
						break;
					}
					
					Log.e(TAG, errorMessage);
					
					AlertDialog.Builder alertBuilder 
						= new AlertDialog.Builder(getApplicationContext());
					alertBuilder.setTitle("Error")
						.setMessage(errorMessage)
						.setCancelable(false);
					AlertDialog alertDialog = alertBuilder.create();
					alertDialog.show();
				}
			}
		}
	}*/
	
	// --------------------------------------------------
	// Save
	public void clickSave(View view) {
		startProgress();
		
		MyApp app = (MyApp)this.getApplicationContext();
		String attendantCode = app.getAttendantCode();
		
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("attendantCode", attendantCode));
		nameValuePairs.add(new BasicNameValuePair("lotNo", txtLotNo.getText().toString()));
		nameValuePairs.add(new BasicNameValuePair("carRegNo", txtCarRegNo.getText().toString()));
		
		new Thread(new SaveRunnable(this, nameValuePairs)).start();
	}
	
	class SaveRunnable implements Runnable {

		List<NameValuePair> _values;
		Context _context;
		
		public SaveRunnable(Context context, List<NameValuePair> values) {
			_context = context;
			_values = values;
		}
		
		@Override
		public void run() {
			try {
				save();
				
				// Toast save ok
				_handler.post(new Runnable() {
					
					@Override
					public void run() {
						Toast.makeText(_context, "Save OK.",
								Toast.LENGTH_SHORT).show();
					}
				});
				
				// clean
				_handler.post(new Runnable() {
					
					@Override
					public void run() {
						txtLotNo.setText("");
						txtCarRegNo.setText("");
						
						Intent intent = new Intent(ChargeActivity.this, PrintActivity.class);
						intent.putExtra("tranCode", _tranCode);
						startActivity(intent);
					}
				});
				
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
				_handler.post(new AlertRunnable(ChargeActivity.this, e.getMessage()));
			} finally {
				_handler.post(new Runnable() {
					
					@Override
					public void run() {
						stopProgress();
					}
				});
			}
		}
		
		private void save() throws Exception {
			FormPost formPost = new FormPost();
			String result = formPost.readJsonFeed(
					"http://10.5.1.14:8080/attendant/charge/devicecreate/", 
					_values);
			
			JSONObject jsonObj;
			try {
				jsonObj = new JSONObject(result);
				
				boolean returnStatus = jsonObj.getBoolean("returnStatus");
				if (returnStatus) {
					Log.d(TAG, "Save ok.");
					
					_tranCode = jsonObj.getString("tranCode");
					
				} else {
					String returnMessage = jsonObj.getString("returnMessage");
					//Log.e(TAG, returnMessage);
					throw new Exception(returnMessage);
				}
				
			} catch (JSONException e) {
				//String errorMessage = e.getMessage();
				//Log.e(TAG, errorMessage);
				throw e;
				
			} catch (Exception e) {
				//String errorMessage = e.getMessage();
				//Log.e(TAG, errorMessage);
				throw e;
			}
		}
		
	}

}
