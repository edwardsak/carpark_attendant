package com.borneoixora.carparkattendant;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class PrintActivity extends Activity {
	
	private static final String TAG = "PrintActivity";
	
	private Button btnRefresh;
	private Button btnReprint;
	
	private String _tranCode;
	private Charge _charge;
	
	private boolean _firstRun;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_print);
		
		btnRefresh = (Button)findViewById(R.id.btnRefresh);
		btnRefresh.setEnabled(false);
		
		btnReprint = (Button)findViewById(R.id.btnReprint);
		btnReprint.setEnabled(false);
		
		_firstRun = true;
		
		_tranCode = getIntent().getStringExtra("tranCode");
		
		getCharge();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.print, menu);
		return true;
	}
	
	// --------------------------------------------------
	// get charge
	public void clickRefresh(View view) {
		getCharge();
	}
	
	private void getCharge() {
		// get charge
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("chargeId", _tranCode));
		
		new GetChargeTask(this, nameValuePairs).execute();
	}
	
	class GetChargeTask extends AsyncTask<Void, Void, String> {

		Context _context;
		List<NameValuePair> _values;
		ProgressDialog _progressDialog;
		
		public GetChargeTask(Context context, List<NameValuePair> values) {
			_context = context;
			_values = values;
			_progressDialog = null;
		}
		
		@Override
		protected void onPreExecute() {
			_progressDialog = new ProgressDialog(_context);
			_progressDialog.setMessage("Please wait...");
			_progressDialog.setIndeterminate(true);
			_progressDialog.setCancelable(false);
			_progressDialog.show();
			
			super.onPreExecute();
		}
		
		@Override
		protected String doInBackground(Void... params) {
			FormPost formPost = new FormPost();
			return formPost.readJsonFeed(
					"http://10.5.1.14:8080/attendant/charge/find/", 
					_values);
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (_progressDialog.isShowing()) {
				_progressDialog.dismiss();
		    }
			
			btnRefresh.setEnabled(true);
			
			JSONObject jsonObj;
			try {
				jsonObj = new JSONObject(result);
				
				boolean returnStatus = jsonObj.getBoolean("returnStatus");
				if (returnStatus) {
					JSONArray jsonArray = jsonObj.getJSONArray("data");
					JSONObject data = jsonArray.getJSONObject(0);
					
					String tranDate = data.getString("tranDate");
					double amt = data.getDouble("amt");
					String lotNo = data.getString("lotNo");
					String carRegNo = data.getString("carRegNo");
					String remark = data.getString("remark");
					
					_charge = new Charge();
					_charge.tranCode = _tranCode;
					_charge.tranDate = tranDate;
					_charge.lotNo = lotNo;
					_charge.carRegNo = carRegNo;
					_charge.remark = remark;
					_charge.amt = amt;
					
					btnReprint.setEnabled(true);
					
					// print receipt at first run
					if (_firstRun) {
						_firstRun = false;
						
						print();
					}
					
				} else {
					String returnMessage = jsonObj.getString("returnMessage");
					Log.e(TAG, returnMessage);
					AlertRunnable.alert(_context, returnMessage);
				}
				
			} catch (JSONException e) {
				Log.e(TAG, e.getMessage());
				AlertRunnable.alert(_context, e.getMessage());
			}
		}
		
	}
	
	// --------------------------------------------------
	// print
	public void clickReprint(View view) {
		print();
	}
	
	private void print() {
		new PrintTask(this, "00:00:05:DB:86:33").execute();
		//printReceiptEsc("00:00:05:DB:86:33");
	}
	
	class PrintTask extends AsyncTask<Void, Void, Boolean> {

		Context _context;
		String _bdAddress;
		String _errorMessage;
		ProgressDialog _progressDialog;
		
		public PrintTask(Context context, String bdAddress) {
			_context = context;
			_bdAddress = bdAddress;
			_errorMessage = "";
			_progressDialog = null;
		}
		
		@Override
		protected void onPreExecute() {
			_progressDialog = new ProgressDialog(_context);
			_progressDialog.setMessage("Please wait...");
			_progressDialog.setIndeterminate(true);
			_progressDialog.setCancelable(false);
			_progressDialog.show();
			
			super.onPreExecute();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				if(!BluetoothHelper.OpenPrinter(_bdAddress))
				{
					_errorMessage = BluetoothHelper.errorMessage;
					return false;
				}
				
				// print
				ZicoxEscHelper.initialize();
				ZicoxEscHelper.setLineGap(0);
				ZicoxEscHelper.write("\n");
				
				ZicoxEscHelper.setAlign(ZicoxEscHelper.ALIGN_LEFT);
				ZicoxEscHelper.setFontSize(ZicoxEscHelper.FONT_SIZE_1XW_2XH);
				ZicoxEscHelper.write(String.format("Doc. No.: %s\n", _charge.tranCode));
				ZicoxEscHelper.write(String.format("Date: %s\n", _charge.tranDate));
				ZicoxEscHelper.write(String.format("Lot No.: %s\n", _charge.lotNo));
				ZicoxEscHelper.write(String.format("Car Reg. No.: %s\n", _charge.carRegNo));
				ZicoxEscHelper.write(String.format("Amt (RM): %.2f\n", _charge.amt));
				
				ZicoxEscHelper.write("\n\n\n\n");
				
				return true;
				
			} catch (Exception e) {
				Log.d(TAG, e.getMessage());
				_errorMessage = e.getMessage();
				return false;
			}
			finally {
				BluetoothHelper.close();
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (_progressDialog.isShowing()) {
				_progressDialog.dismiss();
		    }
			
			if (result) {
				Toast.makeText(_context, "Print OK.", Toast.LENGTH_SHORT).show();
			} else {
				AlertRunnable.alert(_context, _errorMessage);
			}
		}
		
	}
	
	/*private void printReceiptEsc(String bdAddress) {
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
			ZicoxEscHelper.write(String.format("Doc. No.: %s\n", _tranCode));
			
			ZicoxEscHelper.setAlign(ZicoxEscHelper.ALIGN_CENTER);
			ZicoxEscHelper.setFontSize(ZicoxEscHelper.FONT_SIZE_1XW_2XH);
			ZicoxEscHelper.write(String.format("%s\n", "2011-05-16"));
			ZicoxEscHelper.write("\n\n\n\n");
			
			Toast.makeText(this, "Print completed.", Toast.LENGTH_SHORT).show();
			
		} catch (Exception e) {
			Log.d(TAG, e.getMessage());
			
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Error")
				.setMessage(e.getMessage())
				.setCancelable(true);
			AlertDialog dialog = builder.create();
			dialog.show();
		}
		finally {
			BluetoothHelper.close();
		}
	}*/

}
