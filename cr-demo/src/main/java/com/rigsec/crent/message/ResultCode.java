package com.rigsec.crent.message;

public class ResultCode {
	
	private ResultCode(){}
	
	public static final String SUCCESS = "9000";
	public static final String INVALID_CUR = "1001";
	public static final String INVALID_EXCHANGE = "2001";
	public static final String INVALID_PARAM = "3001";
	public static final String SYS_EXCEPTION = "4001";
	public static final String CERTIFY_FAILING = "5001";
	
	public static final String APP_TOKEN_INVALID = "6001";
	public static final String APP_TOKEN_DISABLED = "6002";
	public static final String APP_REPEATED_REQ_SMS = "6003";

}
