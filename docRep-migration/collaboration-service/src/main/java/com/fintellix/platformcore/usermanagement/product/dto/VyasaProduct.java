package com.fintellix.platformcore.usermanagement.product.dto;

import java.io.Serializable;

public class VyasaProduct implements Serializable {
	private static final long serialVersionUID = -483012071802887492L;
	
	private Integer productID;
	private String productName;
	private String productDescription;
	private Boolean isActive;
	public Integer getProductID() {
		return productID;
	}
	public void setProductID(Integer productID) {
		this.productID = productID;
	}
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
	public String getProductDescription() {
		return productDescription;
	}
	public void setProductDescription(String productDescription) {
		this.productDescription = productDescription;
	}
	public Boolean getIsActive() {
		return isActive;
	}
	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
	}
	
}