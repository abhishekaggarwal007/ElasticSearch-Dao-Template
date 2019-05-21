package com.estemplate.common;

import java.util.List;

public class ESResponse<T extends DocumentWrapper> {

	private String status;
	private String documentId;
	private String index;
	private String type;
	private List<T> documentList;

	public ESResponse() {
	}

	public ESResponse(String status, String documentId, String index, String type) {
		super();
		this.status = status;
		this.documentId = documentId;
		this.index = index;
		this.type = type;

	}

	public ESResponse(String status, String documentId, String index, String type, List<T> documentList) {
		super();
		this.status = status;
		this.documentId = documentId;
		this.index = index;
		this.type = type;
		this.documentList = documentList;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getIndex() {
		return index;
	}

	public void setIndex(String index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<T> getDocumentList() {
		return documentList;
	}

	public void setDocumentList(List<T> documentList) {
		this.documentList = documentList;
	}

}
