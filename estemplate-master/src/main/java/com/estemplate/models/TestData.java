package com.estemplate.models;

import com.estemplate.common.DocumentWrapper;

public class TestData implements DocumentWrapper {
	
	private String user;
	private String postDate;
	private String message;
	private String document_id;
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPostDate() {
		return postDate;
	}
	public void setPostDate(String postDate) {
		this.postDate = postDate;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getDocument_id() {
		return document_id;
	}
	public void setDocument_id(String document_id) {
		this.document_id = document_id;
	}
	
	

}
