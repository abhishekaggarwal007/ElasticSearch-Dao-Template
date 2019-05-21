package com.estemplate.dao;

import org.apache.http.util.ExceptionUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import com.estemplate.common.ESResponse;
import com.estemplate.common.EsHelper;

/**
 * @author Abhishek Aggarwal
 */
@SuppressWarnings("deprecation")
public class EsTemplate {

	private static final String FOUND = "Found";
	private static final String NOT_FOUND = "NOT FOUND";
	private static final String DOCUMENT_ID = "NOT FOUND";

	private Logger logger = Logger.getLogger(this.getClass());

	private RestHighLevelClient client = null;

	public EsTemplate(RestHighLevelClient client) {
		this.client = client;
	}

	public <T> ESResponse indexDocument(T t, String index, String mapping, String documentId) {
		IndexRequest request = null;
		if (index != null && !index.isEmpty() && mapping != null && !mapping.isEmpty() && documentId != null
				&& !documentId.isEmpty()) {
			request = new IndexRequest(index, mapping, documentId);
		} else if (documentId == null || documentId.isEmpty()) {
			request = new IndexRequest(index, mapping);

		} else if (mapping == null || mapping.isEmpty()) {
			request = new IndexRequest(index);
		} else {
			logger.error("Index is not specified");
			throw new IllegalArgumentException();
		}
		String jsonString = EsHelper.converttoJsonString(t);
		request.source(jsonString, XContentType.JSON);
		IndexResponse indexResponse = null;
		ESResponse esResponse = new ESResponse();
		try {
			indexResponse = client.index(request, RequestOptions.DEFAULT);
			Result result = indexResponse.getResult();
			if (result != null) {
				esResponse.setStatus(result.toString());
				esResponse.setDocumentId(indexResponse.getId());
				esResponse.setIndex(indexResponse.getIndex());
				esResponse.setType(indexResponse.getType());
				return esResponse;

			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public <T> T getFromIndex(Class<T> entityClass, String index, String mapping, String documentId) {
		String doc = null;
		GetRequest getRequest = new GetRequest(index, mapping, documentId);
		GetResponse response = null;
		try {
			response = client.get(getRequest, RequestOptions.DEFAULT);
			if (response.isExists()) {
				doc = response.getSourceAsString();
				if (doc != null && !doc.isEmpty()) {
					return EsHelper.convertfromJsonString(doc, entityClass);
				}
			} else {
				logger.info("No document exists with this document Id: " + documentId);
			}
		} catch (ElasticsearchException e) {
			if (e.status() == RestStatus.NOT_FOUND) {
				logger.error("Index does not exists: " + index);
			}
			if (e.status() == RestStatus.CONFLICT) {
				logger.error("Existing document has a different version number: " + documentId);
			}
		} catch (Exception e) {
			logger.error(e);
		}
		return null;
	}

	public ESResponse isDocumentExists(String index, String mapping, String documentId) {
		ESResponse esResponse = new ESResponse();
		boolean exists = false;
		GetRequest getRequest = new GetRequest(index, mapping, documentId);
		getRequest.fetchSourceContext(new FetchSourceContext(false));
		getRequest.storedFields("_none_");
		try {
			exists = client.exists(getRequest, RequestOptions.DEFAULT);
		} catch (Exception e) {
			logger.error(e);
		}
		if (exists) {
			esResponse.setStatus(FOUND);
		} else
			esResponse.setStatus(NOT_FOUND);
		return esResponse;
	}

	public ESResponse deleteDocument(String index, String mapping, String documentId) {
		ESResponse esResponse = new ESResponse();
		DeleteResponse deleteResponse = null;
		DeleteRequest request = new DeleteRequest(index, mapping, documentId);
		try {
			deleteResponse = client.delete(request, RequestOptions.DEFAULT);
			esResponse.setStatus(deleteResponse.getResult().toString());
		} catch (Exception e) {
			logger.error(e);
		}

		return esResponse;
	}
	
	

	private Object getValueForType(String valueToBeSet, String type) {

		switch (type) {

		case "String":
			return valueToBeSet;

		case "short":
		case "Short":
			return Short.valueOf(valueToBeSet);

		case "long":
		case "Long":
			return Long.valueOf(valueToBeSet);

		case "int":
		case "Integer":
			return Integer.valueOf(valueToBeSet);

		case "double":
		case "Double":
			return Double.valueOf(valueToBeSet);

		case "float":
		case "Float":
			return Float.valueOf(valueToBeSet);

		case "boolean":
		case "Boolean":
			return Boolean.valueOf(valueToBeSet);

		default:
			throw new IllegalArgumentException("Unsupported Field Type: " + type + " Value: [" + valueToBeSet + "]");
		}

	}
}