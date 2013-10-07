package com.borneoixora.carparkattendant;

import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.wpx.bean.ResultInfo;
import com.wpx.myutil.PlaySoundUtil;
import com.wpx.service.IUHFService;
import com.wpx.service.impl.UHFServiceImpl;
import com.wpx.util.GlobalUtil;

public class TestUhfActivity extends Activity {
	
	private static final String TAG = "TestUhfActivity";

	private IUHFService uhfService = new UHFServiceImpl();
	
	TextView txtEpcId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_uhf);
		
		txtEpcId = (TextView)findViewById(R.id.txtEpcId);
		
		// set device type to cm390
		uhfService.setDeviceType(0);
		
		// set tag type to epc g2
		uhfService.setTagType(GlobalUtil.op.Tag_Type);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.test_uhf, menu);
		return true;
	}
	
	public void clickConnect(View view) {
		new ConnectTask().execute();
	}
	
	public void clickDisconnect(View view) {
		new DisconnectTask().execute();
	}
	
	public void clickScan(View view) {
		new ScanTask().execute();
	}
	
	class ConnectTask extends AsyncTask<Integer, String, Integer> {

		ResultInfo resultInfo = null;
		String version;
		
		//@Override
		protected void onPreExecute() {
			try {
				connect();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
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
		
		private void connect() {
			int type = GlobalUtil.dev_type;
			uhfService.disConnected();
			if(type == 1)
				resultInfo = uhfService.connected(1);
			else{
				resultInfo = uhfService.connected(0);
			}
		}
		
		//@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			
			if(resultInfo !=null) {
				if(resultInfo.getResult() == 0) {
					Log.d(TAG, "Connected");
					Toast.makeText(getApplicationContext(), "Connected. Version=" + version,
							Toast.LENGTH_SHORT).show();
				} else {
					String errorMessage = "Open port is failed : " + resultInfo.getResult();
					Log.e(TAG, errorMessage);
					Toast.makeText(getApplicationContext(), errorMessage,
							Toast.LENGTH_SHORT).show();
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
			Toast.makeText(getApplicationContext(), "Disconnected",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	class ScanTask extends AsyncTask<Integer,String,Integer> {
		
		ResultInfo resultInfo = null;
		
		//@Override
		protected Integer doInBackground(Integer... params) {
			resultInfo = uhfService.getEPCList(uhfService.getQValue());
			return 0;
		}
		
		//@Override
		protected void onPreExecute() {
			// scanning
		}
		
		//@Override
		protected void onPostExecute(Integer result) {
			if (resultInfo!=null) {
				if (resultInfo.getResult() == 0) {
					List<String> values = resultInfo.getValues();
					if (values.size() > 0) {
						String epcId = values.get(0);
						Log.d(TAG, epcId);
						txtEpcId.setText(epcId);
						
						PlaySoundUtil.play();	
					}
				} else {
					String errorMessage = "Search tag is failed : " + resultInfo.getResult();
					Log.e(TAG, errorMessage);
					Toast.makeText(getApplicationContext(), errorMessage,
							Toast.LENGTH_SHORT).show();
				}
			}
		}
	}

}
