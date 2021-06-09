package com.fintellix.dld.util;

public class LineItemInfo{
	public String getLineItemId() {
		return lineItemId;
	}
	
	public void setLineItemId(String lineItemId) {
		this.lineItemId = lineItemId;
	}
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getStamp() {
		return stamp;
	}
	
	public void setStamp(String stamp) {
		this.stamp = stamp;
	}
	
	private String lineItemId;
	private String status;
	private String stamp;
	
}
