package com.borneoixora.carparkattendant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class PrintActivity extends Activity {
	
	private static final String TAG = "PrintActivity";
	
	String _tranCode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_print);
		
		_tranCode = getIntent().getStringExtra("tranCode");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.print, menu);
		return true;
	}
	
	public void clickReprint(View view) {
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
				ZicoxEscHelper.write(String.format("Doc. No.: %s\n", _tranCode));
				
				ZicoxEscHelper.setAlign(ZicoxEscHelper.ALIGN_CENTER);
				ZicoxEscHelper.setFontSize(ZicoxEscHelper.FONT_SIZE_1XW_2XH);
				ZicoxEscHelper.write(String.format("%s\n", "2011-05-16"));
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
