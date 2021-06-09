package com.fintellix.dld.util;

import java.util.List;

public class ReportStatusPayload {
	
	private String solutionId;
	private String fetchAllLineItems;
	private List<ReportInfo> reportInfo;
	
	public String getSolutionId() {
		return solutionId;
	}
	

	public void setSolutionId(String solutionId) {
		this.solutionId = solutionId;
	}
	

	public String getFetchAllLineItems() {
		return fetchAllLineItems;
	}
	

	public void setFetchAllLineItems(String fetchAllLineItems) {
		this.fetchAllLineItems = fetchAllLineItems;
	}
	

	public List<ReportInfo> getReportInfo() {
		return reportInfo;
	}
	

	public void setReportInfo(List<ReportInfo> reportInfo) {
		this.reportInfo = reportInfo;
	}

	
}
