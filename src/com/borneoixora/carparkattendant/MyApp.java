package com.borneoixora.carparkattendant;

import android.app.Application;

public class MyApp extends Application {
	private String attendantCode;
	
	public String getAttendantCode() {
		return attendantCode;
	}
	
	public void setAttendantCode(String value) {
		attendantCode = value;
	}
	
}
