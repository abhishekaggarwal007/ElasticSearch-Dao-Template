package com.estemplate.dao;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.DocWriteResponse.Result;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;

import com.estemplate.common.DocumentWrapper;
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

	public <T> ESResponse addUpdateDocument(T t, String index, String mapping, String documentId) {
		IndexRequest indexRequest = null;
		UpdateRequest request = null;
		String jsonString = EsHelper.converttoJsonString(t);
		if (index != null && !index.isEmpty() && mapping != null && !mapping.isEmpty() && documentId != null
				&& !documentId.isEmpty()) {
			indexRequest = new IndexRequest(index, mapping, documentId).source(jsonString, XContentType.JSON);
			request = new UpdateRequest(index, mapping, documentId).upsert(indexRequest);
			request.doc(indexRequest);

			UpdateResponse updateResponse = null;
			ESResponse esResponse = null;
			try {
				updateResponse = client.update(request, RequestOptions.DEFAULT);
				esResponse = new ESResponse(updateResponse.getIndex(), updateResponse.getType(), updateResponse.getId(),
						updateResponse.getResult().toString());
			} catch (Exception e) {
				logger.error(e);
			}
			return esResponse;
		}
		return null;
	}
	
	public <T extends DocumentWrapper> ESResponse bulkUpsert(List<T> docList, String index, String mapping) {
		ESResponse esResponse = new ESResponse();
		BulkRequest request = new BulkRequest();
		IndexRequest indexRequest = null;
		UpdateRequest updateRequest = null;
		String jsonString = null;
		for(T t: docList) {
			if(t.getDocument_id()!=null && t.getDocument_id()!="") {
				jsonString = EsHelper.converttoJsonString(t);
				indexRequest = new IndexRequest(index,mapping,t.getDocument_id()).source(jsonString,XContentType.JSON);
				updateRequest = new UpdateRequest(index,mapping,t.getDocument_id()).upsert(indexRequest);
				updateRequest.doc(indexRequest);
				request.add(updateRequest);
			}
		}
		try {
			BulkResponse bulkResponse = client.bulk(request,RequestOptions.DEFAULT);
			esResponse.setStatus(!bulkResponse.hasFailures()?"SUCCESS":"FALIURE");
			esResponse.setIndex(index);
			esResponse.setType(mapping);
		}
		catch(Exception e) {
			logger.error(e);
		}
		return esResponse;
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

	public <T extends DocumentWrapper> ESResponse getMultiple(Class<T> entityClass, String index, String mapping,
			String[] ids) {
		ESResponse esResponse = null;
		if (index != null && !index.isEmpty() && mapping != null && !mapping.isEmpty() && ids != null
				&& ids.length > 0) {
			MultiGetRequest request = new MultiGetRequest();
			Arrays.stream(ids).forEach(id -> request.add(new MultiGetRequest.Item(index, mapping, id)));
			try {
				MultiGetResponse response = client.mget(request, RequestOptions.DEFAULT);
				MultiGetItemResponse[] items = response.getResponses();
				List<T> docList = new LinkedList<T>();
				for (MultiGetItemResponse resp : items) {
					GetResponse getResponse = resp.getResponse();
					if (getResponse.isExists()) {
						String sourceAsString = getResponse.getSourceAsString();
						T doc = EsHelper.convertfromJsonString(sourceAsString, entityClass);
						doc.setDocument_id(resp.getId());
						docList.add(doc);
					}
				}
				esResponse = new ESResponse(index, mapping, null, null, docList);

			} catch (Exception e) {
				logger.error(e);
			}
		}
		return esResponse;
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

	/************************************
	 * **********************************************************************************
	 */

	public <T extends AbstractAggregationBuilder> SearchResponse querywithAggregation(QueryBuilder query, T aggregation,
			int from, int size, String... indices) {
		SearchRequest searchrequest = new SearchRequest(indices);
		SearchSourceBuilder searchBuilder = new SearchSourceBuilder();
		searchBuilder.size(size);
		searchBuilder.from(from);

		if (query != null) {
			searchBuilder.query(query);
		}
		if (aggregation != null) {
			searchBuilder.aggregation(aggregation);
		}
		searchrequest.source(searchBuilder);
		SearchResponse searchResponse = null;
		try {
			searchResponse = client.search(searchrequest, RequestOptions.DEFAULT);
		} catch (Exception e) {
			logger.error(e);
		}
		return searchResponse;
	}

	public <T> List<T> query(Class<T> entityClass, QueryBuilder query, int from, int size, String... indices)
			throws InstantiationException, IllegalAccessException {
		Map<String, Field> fieldsMap = getFieldsMap(entityClass);
		List<T> queryResults = new ArrayList<T>();
		SearchResponse resp = querywithAggregation(query, null, from, size, indices);
		SearchHits hits = resp.getHits();
		if (hits != null) {
			for (SearchHit result : hits.getHits()) {
				prepareResults(entityClass, fieldsMap, queryResults, result);
			}
		}
		return queryResults;
	}

	private <T> void prepareResults(Class<T> entityClass, Map<String, Field> fieldsMap, List<T> queryResults,
			SearchHit result) throws InstantiationException, IllegalAccessException {
		T t = (T) entityClass.newInstance();
		Set<String> fieldNameSet = fieldsMap.keySet();
		Map<String, Object> sourceAsMap = result.getSourceAsMap();

		for (String fieldKey : fieldNameSet) {
			Field field = fieldsMap.get(fieldKey);
			Object convertedValue = null;
			Object valueToBeSet = sourceAsMap.getOrDefault(fieldKey, "");
			if (valueToBeSet instanceof List || valueToBeSet instanceof java.util.Collection) {
				convertedValue = valueToBeSet;
			} else {
				convertedValue = getValueForType(((String) valueToBeSet), field.getType().getSimpleName());
			}
			field.set(t, convertedValue);

		}
		queryResults.add(t);

	}

	private <T> Map<String, Field> getFieldsMap(Class<T> entityClass) {
		Map<String, Field> fieldsMap = new HashMap<String, Field>();
		Field[] fields = entityClass.getDeclaredFields();
		for (Field field : fields) {
			field.setAccessible(true);
			fieldsMap.put(field.getName(), field);
		}
		return fieldsMap;
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