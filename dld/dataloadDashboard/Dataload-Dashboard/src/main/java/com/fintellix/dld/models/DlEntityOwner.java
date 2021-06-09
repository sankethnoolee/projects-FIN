package com.fintellix.dld.models;

import java.io.Serializable;

public class DlEntityOwner implements Serializable {
	private static final long serialVersionUID = 1L;

	private String clientCode;
	private String owner_Name;
	private String description;
	private String external_Source;
	private Integer data_Source_Id;
	private Integer solution_Id;
	private String contact_Details;
	private String display_Sorting_Order;
	
	public Integer getData_Source_Id() {
		return data_Source_Id;
	}
	public void setData_Source_Id(Integer data_Source_Id) {
		this.data_Source_Id = data_Source_Id;
	}
	public Integer getSolution_Id() {
		return solution_Id;
	}
	public void setSolution_Id(Integer solution_Id) {
		this.solution_Id = solution_Id;
	}
	
	
	public String getClientCode() {
		return clientCode;
	}
	public void setClientCode(String clientCode) {
		this.clientCode = clientCode;
	}
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
