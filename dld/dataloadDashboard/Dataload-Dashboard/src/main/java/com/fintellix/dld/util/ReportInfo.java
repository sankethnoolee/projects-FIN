package com.fintellix.dld.util;

import java.util.List;

public class ReportInfo{
	private String periodId;
	private String regReportId;
	private String lineItemIds;
	private String status;
	private String stamp;
	private List<LineItemInfo> lineitems;
	public String getPeriodId() {
		return periodId;
	}
	
	public void setPeriodId(String periodId) {
		this.periodId = periodId;
	}
	
	public String getRegReportId() {
		return regReportId;
	}
	
	public void setRegReportId(String regReportId) {
		this.regReportId = regReportId;
	}
	
	public String getLineItemIds() {
		return lineItemIds;
	}
	
	public void setLineItemIds(String lineItemIds) {
		this.lineItemIds = lineItemIds;
	}

	public String getStamp() {
		return stamp;
	}

	public void setStamp(String stamp) {
		this.stamp = stamp;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public List<LineItemInfo> getLineitems() {
		return lineitems;
	}

	public void setLineitems(List<LineItemInfo> lineitems) {
		this.lineitems = lineitems;
	}
	
}
