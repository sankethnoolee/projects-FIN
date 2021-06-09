package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;

public class DIMbkeys implements Serializable, Comparable<DIMbkeys>{
	private static final long serialVersionUID = 1L;

	// id, data_source_id, desc, bkey, datasourcename
	
	private String idColumn;
	private Integer dataSourceIdCol;
	private String dimensionBkeyCol;
	private String dimensionDescCol;
	private String dataSourceName;
	private String isInherited;
	private String isDeleted;

	
	public String getIdColumn() {
		return idColumn;
	}
	public void setIdColumn(String idColumn) {
		this.idColumn = idColumn;
	}
	public Integer getDataSourceIdCol() {
		return dataSourceIdCol;
	}
	public void setDataSourceIdCol(Integer dataSourceIdCol) {
		this.dataSourceIdCol = dataSourceIdCol;
	}
	public String getDimensionBkeyCol() {
		return dimensionBkeyCol;
	}
	public void setDimensionBkeyCol(String dimensionBkeyCol) {
		this.dimensionBkeyCol = dimensionBkeyCol;
	}
	public String getDimensionDescCol() {
		return dimensionDescCol;
	}
	public void setDimensionDescCol(String dimensionDescCol) {
		this.dimensionDescCol = dimensionDescCol;
	}

	public String getDataSourceName() {
		return dataSourceName;
	}
	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}
	public String getIsInherited() {
		return isInherited;
	}
	public void setIsInherited(String isInherited) {
		this.isInherited = isInherited;
	}
	public String getIsDeleted() {
		return isDeleted;
	}
	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dataSourceName == null) ? 0 : dataSourceName.toUpperCase().hashCode());
		result = prime
				* result
				+ ((dimensionBkeyCol == null) ? 0 : dimensionBkeyCol.toUpperCase().hashCode());
		return result;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DIMbkeys other = (DIMbkeys) obj;
		if (dataSourceName == null) {
			if (other.dataSourceName != null)
				return false;
		} else if (!dataSourceName.equalsIgnoreCase(other.dataSourceName))
			return false;
		if (dimensionBkeyCol == null) {
			if (other.dimensionBkeyCol != null)
				return false;
		} else if (!dimensionBkeyCol.equalsIgnoreCase(other.dimensionBkeyCol))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "DIMbkeys [idColumn=" + idColumn + ", dataSourceIdCol="
				+ dataSourceIdCol + ", dimensionBkeyCol=" + dimensionBkeyCol
				+ ", dimensionDescCol=" + dimensionDescCol
				+ ", dataSourceName=" + dataSourceName + ", isInherited="
				+ isInherited + ", isDeleted=" + isDeleted + "]";
	}
	
	@Override
	public int compareTo(DIMbkeys dimBkeys) {
        String bkey=((DIMbkeys)dimBkeys).getDimensionBkeyCol();
        /* For Ascending order*/
        return (this.dimensionBkeyCol.compareTo(bkey));
    }
	
}
