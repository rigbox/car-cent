package com.rigsec.crent.message;

import java.util.List;

public class ResultDataTable {
	
	private long recordsTotal;
	private long recordsFiltered;
	private int draw;
	private List<?> data;
	
	public ResultDataTable(long total, int draw, List<?> list) {
		this.recordsTotal = total;
		this.recordsFiltered = total;
		this.draw = draw;
		this.data = list;
	}
	
	public long getRecordsTotal() {
		return recordsTotal;
	}
	public void setRecordsTotal(long recordsTotal) {
		this.recordsTotal = recordsTotal;
	}
	public long getRecordsFiltered() {
		return recordsFiltered;
	}
	public void setRecordsFiltered(long recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}
	public int getDraw() {
		return draw;
	}
	public void setDraw(int draw) {
		this.draw = draw;
	}

	public List<?> getData() {
		return data;
	}

	public void setData(List<?> data) {
		this.data = data;
	}

}
