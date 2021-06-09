package com.fintellix.dld.models;

import java.io.Serializable;

public class DldSolution implements Serializable {
		private static final long serialVersionUID = -483012071802887492L;
		private Integer productID;
		private Integer solutionID;
		private String solutionName;
		private String solutionDescription;
		private String belongsTo;
		private String biType;
		private Boolean isActive;
		private Boolean isSecurityFilterActive;
		public Integer getProductID() {
			return productID;
		}
		public void setProductID(Integer productID) {
			this.productID = productID;
		}
		public Integer getSolutionID() {
			return solutionID;
		}
		public void setSolutionID(Integer solutionID) {
			this.solutionID = solutionID;
		}
		public String getSolutionName() {
			return solutionName;
		}
		public void setSolutionName(String solutionName) {
			this.solutionName = solutionName;
		}
		public String getSolutionDescription() {
			return solutionDescription;
		}
		public void setSolutionDescription(String solutionDescription) {
			this.solutionDescription = solutionDescription;
		}
		public String getBelongsTo() {
			return belongsTo;
		}
		public void setBelongsTo(String belongsTo) {
			this.belongsTo = belongsTo;
		}
		public Boolean getIsActive() {
			return isActive;
		}
		public void setIsActive(Boolean isActive) {
			this.isActive = isActive;
		}
		public String getBiType() {
			return biType;
		}
		public void setBiType(String biType) {
			this.biType = biType;
		}
		public Boolean getIsSecurityFilterActive() {
			return isSecurityFilterActive;
		}
		public void setIsSecurityFilterActive(Boolean isSecurityFilterActive) {
			this.isSecurityFilterActive = isSecurityFilterActive;
		}
	}

