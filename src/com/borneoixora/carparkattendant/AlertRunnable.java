package com.borneoixora.carparkattendant;

import android.app.AlertDialog;
import android.content.Context;

class AlertRunnable implements Runnable {

	private Context _context;
	private String _errorMessage;
	
	public AlertRunnable(Context context, String errorMessage) {
		_context = context;
		_errorMessage = errorMessage;
	}
	
	@Override
	public void run() {
		AlertRunnable.alert(_context, _errorMessage);
	}

	public static void alert(Context context, String message) {
		AlertDialog.Builder alertBuilder 
			= new AlertDialog.Builder(context);
		alertBuilder.setTitle("Error")
			.setMessage(message)
			.setCancelable(true);
		AlertDialog alertDialog = alertBuilder.create();
		alertDialog.show();
	}
	
}
