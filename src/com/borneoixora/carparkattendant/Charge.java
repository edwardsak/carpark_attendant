package com.borneoixora.carparkattendant;

public class Charge {
	public String tranCode;
	public String tranDate;
	public String lotNo;
	public String carRegNo;
	public String remark;
	public double amt;
	
	public Charge() {
		tranCode = "";
		tranDate = "";
		lotNo = "";
		carRegNo = "";
		remark = "";
		amt = 0;
	}
}
