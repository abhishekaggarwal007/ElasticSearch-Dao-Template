package com.estemplate.models;

import com.estemplate.common.DocumentWrapper;

public class TestData implements DocumentWrapper {
	
	private String train;
	private String postDate;
	private String status;
	private String document_id;
	
	public String getTrain() {
		return train;
	}
	public void setTrain(String train) {
		this.train = train;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getPostDate() {
		return postDate;
	}
	public void setPostDate(String postDate) {
		this.postDate = postDate;
	}
	
	public String getDocument_id() {
		return document_id;
	}
	public void setDocument_id(String document_id) {
		this.document_id = document_id;
	}
	
	

}
