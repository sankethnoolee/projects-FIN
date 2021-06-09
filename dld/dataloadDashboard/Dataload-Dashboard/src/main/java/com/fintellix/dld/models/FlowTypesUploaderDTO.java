package com.fintellix.dld.models;

import java.io.Serializable;

public class FlowTypesUploaderDTO  implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private String flow_type;
	private String description;
	public String getFlow_type() {
		return flow_type;
	}
	public void setFlow_type(String flow_type) {
		this.flow_type = flow_type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	
	

}
