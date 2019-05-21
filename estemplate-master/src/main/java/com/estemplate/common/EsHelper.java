package com.estemplate.common;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class EsHelper {
	
	public static <T> String converttoJsonString(T t) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		String jsonString = null;
		try {
			jsonString = objectMapper.writeValueAsString(t);
		}
		catch(JsonProcessingException e1) {
			e1.printStackTrace();
		}
		return jsonString;
	}
	
	public static <T> T convertfromJsonString(String jsonString,Class<T> t) {
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		try {
			return objectMapper.readValue(jsonString,t);
		}
		catch(IOException ex) {
			ex.printStackTrace();
		}
		
		return null;
	}
	

}
