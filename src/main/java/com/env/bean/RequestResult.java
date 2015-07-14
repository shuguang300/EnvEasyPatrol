package com.env.bean;

import com.google.gson.Gson;

/**
 * 请求网络后返回的对象
 * 包括errorcode，errormsg等等
 * @author lsg
 *
 */
public class RequestResult {
	
	public static final int NO_ERROR = 1;
	public static final int SERVER_ERROR = 2;
	public static final int EXCEPTION_ERROR =3;
	public static final int IOEXCEPTION_ERROR =4;

	private int errorcode = 0;
	private String errormsg;
	private String data;
	public int getErrorcode() {
		return errorcode;
	}
	public void setErrorcode(int errorcode) {
		this.errorcode = errorcode;
	}
	public String getErrormsg() {
		return errormsg;
	}
	public void setErrormsg(String errormsg) {
		this.errormsg = errormsg;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}



	
}
