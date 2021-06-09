package com.fintellix.dld.models;

import java.io.Serializable;

public class TaskFrequencyExclusionOffset  implements Serializable{
	
private static final long serialVersionUID = 1L;

private String frequency;
private Integer offset;
public String getFrequency() {
	return frequency;
}
public void setFrequency(String frequency) {
	this.frequency = frequency;
}
public Integer getOffset() {
	return offset;
}
public void setOffset(Integer offset) {
	this.offset = offset;
}



}
