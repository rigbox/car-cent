package com.rigsec.crent.message;

public class ResultResponse {
	
	private String code;
	
	private String message;
	
	private Long total;
	
	private Object data;
	
	public ResultResponse(String code, String message, Object data) {
		this.code = code;
		this.message = message;
		this.data = data;
	}
	
	public ResultResponse(String code,String message,Long total,Object data) {
		this.code = code;
		this.message = message;
		this.total = total;
		this.data = data;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}
	
}
