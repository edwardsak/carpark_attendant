package com.borneoixora.carparkattendant;

import java.io.UnsupportedEncodingException;

public class ZicoxEscHelper {
	private static final byte ESC = 0x1B;
	private static final byte GS = 0x1D;
	public static final int FONT_SIZE_1XW_1XH = 0;
	public static final int FONT_SIZE_1XW_2XH = 1;
	public static final int FONT_SIZE_1XW_3XH = 2;
	public static final int FONT_SIZE_2XW_1XH = 16;
	public static final int FONT_SIZE_2XW_2XH = 17;
	public static final int FONT_SIZE_2XW_3XH = 18;
	public static final int FONT_SIZE_3XW_1XH = 32;
	public static final int FONT_SIZE_3XW_2XH = 33;
	public static final int FONT_SIZE_3XW_3XH = 34;
	public static final int ALIGN_LEFT = 0;
	public static final int ALIGN_CENTER = 1;
	public static final int ALIGN_RIGHT = 2;
	
	public static void initialize() {
		// ESC @
		// clear print buffer
		BluetoothHelper.write(new byte[] {ESC, 0x40});
	}
	
	public static void setLineGap(int n) {
		// ESC 3 n, set line gap
		BluetoothHelper.write(new byte[] {ESC, 0x33, (byte)n});
	}
	
	public static void setAlign(int n) {
		// ESC a n, set align
		// 0 = left
		// 1 = center
		// 2 = right
		BluetoothHelper.write(new byte[] {ESC, 0x61, (byte)n});
	}
	
	public static void setFontSize(int n) {
		// GS ! n, set font size
		// 0b000000 = 1x height, 1x width, 0
		// 0b000001 = 2x height, 1x width, 1
		// 0b000010 = 3x height, 1x width, 2
		// 0b010000 = 1x height, 2x width, 16
		// 0b010001 = 2x height, 2x width, 17
		// 0b010010 = 3x height, 2x width, 18
		// 0b100000 = 1x height, 3x width, 32
		// 0b100001 = 2x height, 3x width, 33
		// 0b100010 = 3x height, 3x width, 34
		BluetoothHelper.write(new byte[] {GS, 0x21, (byte)n});
	}
	
	public static void write(byte[] data) {
		BluetoothHelper.write(data);
	}
	
	public static void write(String data) {
		BluetoothHelper.write(data.getBytes());
	}
	
	public static void write(String data, String charsetName) 
			throws UnsupportedEncodingException {
		BluetoothHelper.write(data.getBytes(charsetName));
	}
	
}
