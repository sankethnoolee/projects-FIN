package com.fintellix.framework.collaboration.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CollaborationNode implements Serializable{
	private static final long serialVersionUID = 6643484341807167841L;
	public CollaborationNode(String nodeBusinessName,String nodeId,String parentPathUUID){
		List<CollaborationNode> children = new ArrayList<CollaborationNode>();
		this.childrenList = children;
		this.nodeBusinessName = nodeBusinessName;
		this.nodeId = nodeId;
		this.parentPathUUID = parentPathUUID;
	}
	private List<CollaborationNode> childrenList;
	private String nodeBusinessName;
	private String nodeId;
	private String parentPathUUID;
	
	public String getParentPathUUID() {
		return parentPathUUID;
	}
	public void setParentPathUUID(String parentPathUUID) {
		this.parentPathUUID = parentPathUUID;
	}
	public List<CollaborationNode> getChildrenList() {
		return childrenList;
	}
	public void setChildrenList(List<CollaborationNode> childrenList) {
		this.childrenList = childrenList;
	}
	public String getNodeBusinessName() {
		return nodeBusinessName;
	}
	public void setNodeBusinessName(String nodeBusinessName) {
		this.nodeBusinessName = nodeBusinessName;
	}
	public String getNodeId() {
		return nodeId;
	}
	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
	
}
