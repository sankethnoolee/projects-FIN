package com.fintellix.dld.models;

import java.io.Serializable;

public class EntityOwnerUploaderDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private String owner_Name;
	private String description;
	private String external_Source;
	private String data_source_Name;
	private String solution_Name;
	private String contact_Details;
	private String display_Sorting_Order;
	public String getOwner_Name() {
		return owner_Name;
	}
	public void setOwner_Name(String owner_Name) {
		this.owner_Name = owner_Name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getExternal_Source() {
		return external_Source;
	}
	public void setExternal_Source(String external_Source) {
		this.external_Source = external_Source;
	}
	public String getData_source_Name() {
		return data_source_Name;
	}
	public void setData_source_Name(String data_source_Name) {
		this.data_source_Name = data_source_Name;
	}
	public String getSolution_Name() {
		return solution_Name;
	}
	public void setSolution_Name(String solution_Name) {
		this.solution_Name = solution_Name;
	}
	public String getContact_Details() {
		return contact_Details;
	}
	public void setContact_Details(String contact_Details) {
		this.contact_Details = contact_Details;
	}
	public String getDisplay_Sorting_Order() {
		return display_Sorting_Order;
	}
	public void setDisplay_Sorting_Order(String display_Sorting_Order) {
		this.display_Sorting_Order = display_Sorting_Order;
	}

	
	
	
}
