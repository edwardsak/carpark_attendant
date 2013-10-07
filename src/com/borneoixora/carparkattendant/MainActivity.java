package com.borneoixora.carparkattendant;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {
	
	public static final String TAG = "MainActivity";

	EditText txtCode;
	EditText txtPwd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		txtCode = (EditText)findViewById(R.id.txtCode);
		txtPwd = (EditText)findViewById(R.id.txtPwd);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void clickLogin(View view) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("code", txtCode.getText().toString()));
		nameValuePairs.add(new BasicNameValuePair("pwd", txtPwd.getText().toString()));
		
		new LoginTask(this, nameValuePairs).execute();
	}
	
	class LoginTask extends AsyncTask<Void, Void, String> {

		Context _context;
		List<NameValuePair> _values;
		ProgressDialog _progressDialog;
		
		public LoginTask(Context context, List<NameValuePair> values) {
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
					"http://10.5.1.14:8080/attendant/account/login/", 
					_values);
		}
		
		@Override
		protected void onPostExecute(String result) {
			if (_progressDialog.isShowing()) {
				_progressDialog.dismiss();
		    }
			
			JSONObject jsonObj;
			try {
				jsonObj = new JSONObject(result);
				
				boolean returnStatus = jsonObj.getBoolean("returnStatus");
				if (returnStatus) {
					MyApp app = (MyApp)_context.getApplicationContext();
					app.setAttendantCode(txtCode.getText().toString());
					
					Intent intent = new Intent(MainActivity.this, ChargeActivity.class);
					_context.startActivity(intent);
					
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

}
