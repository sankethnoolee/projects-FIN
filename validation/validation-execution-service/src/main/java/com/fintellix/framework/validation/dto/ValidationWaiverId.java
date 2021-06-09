package com.fintellix.framework.validation.dto;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ValidationWaiverId implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "WAIVER_ID")
	private String waiverId;
	
	@Column(name = "SEQUENCE_NO")
	private Integer sequenceNo;

	public String getWaiverId() {
		return waiverId;
	}

	public void setWaiverId(String waiverId) {
		this.waiverId = waiverId;
	}

	public Integer getSequenceNo() {
		return sequenceNo;
	}

	public void setSequenceNo(Integer sequenceNo) {
		this.sequenceNo = sequenceNo;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sequenceNo == null) ? 0 : sequenceNo.hashCode());
		result = prime * result + ((waiverId == null) ? 0 : waiverId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValidationWaiverId other = (ValidationWaiverId) obj;
		if (sequenceNo == null) {
			if (other.sequenceNo != null)
				return false;
		} else if (!sequenceNo.equals(other.sequenceNo))
			return false;
		if (waiverId == null) {
			if (other.waiverId != null)
				return false;
		} else if (!waiverId.equals(other.waiverId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ValidationWaiverId [waiverId=" + waiverId + ", sequenceNo=" + sequenceNo + "]";
	}
	
	
	
}
