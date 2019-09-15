package com.rigsec.crent.controller;

import com.rigsec.crent.message.ResultCode;
import com.rigsec.crent.message.ResultDataTable;
import com.rigsec.crent.message.ResultResponse;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

public abstract class BaseController {

	@Autowired
	protected HttpServletRequest request;
	
	@Autowired
	protected HttpServletResponse response;

	protected int start = 0;
	protected int length = 10;
	protected int draw;
	protected String search;

	
	public ResultResponse resultSuccess() {
		return new ResultResponse(ResultCode.SUCCESS, "Success!", null);
	}
	
	public ResultResponse resultSuccess(Object data) {
		return new ResultResponse(ResultCode.SUCCESS, "Success!",data);
	}

	public ResultDataTable resultDataTable(long total, List<?> list) {
		return new ResultDataTable(total,this.draw,list);
	}
	
	public ResultResponse resultSuccess(Long total, Object data) {
		return new ResultResponse(ResultCode.SUCCESS, "Success!", total, data);
	}
	
	public ResultResponse resultFailed(String code) {
		return new ResultResponse(code, "Failing!", null);
	}

	public ResultResponse resultFailed(String code, String message) {
		return new ResultResponse(code, message, null);
	}
	
	/**
	 * 获取客户端IP
	 * 
	 * @return
	 */
	public String getRemortIP() {
		String ip = this.request.getHeader("x-forwarded-for");
		if (!this.checkIp(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (!this.checkIp(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (!this.checkIp(ip)) {
			ip = request.getHeader("X-Real-IP");
		}
		if (!this.checkIp(ip)) {
			ip = this.request.getRemoteAddr();
		}
		return ip;
	}

	protected boolean checkIp(String ip) {
		if (null == ip || "".equals(ip.trim()) || "unkown".equalsIgnoreCase(ip.trim())) {
			return false;
		}
		return true;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getDraw() {
		return draw;
	}

	public void setDraw(int draw) {
		this.draw = draw;
	}

	public String getSearch() {
		return search;
	}

	public void setSearch(String search) {
		this.search = search;
	}
}
