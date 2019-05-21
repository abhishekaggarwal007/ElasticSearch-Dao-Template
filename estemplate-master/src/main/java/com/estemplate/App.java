package com.estemplate;

import java.io.IOException;
import java.util.Properties;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;

import com.estemplate.dao.EsTemplate;
import com.estemplate.models.TestData;

public class App {

	public static void main(String[] args) throws IOException {

		Properties properties = new Properties();
		properties.load(App.class.getResourceAsStream("/application.properties"));

		RestClientBuilder builder = null;
		Header[] headers = { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json") };
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(
				properties.getProperty("es.username"), properties.getProperty("es.password")));
		String esNodes = properties.getProperty("es.nodes");
		String[] clusterNodes = esNodes.split(",");
		HttpHost hostArray[] = new HttpHost[clusterNodes.length];
		for (int i = 0; i < clusterNodes.length; i++) {
			String ipAddress = clusterNodes[i];
			String[] ipPortPair = ipAddress.split(":");
			HttpHost host = new HttpHost(ipPortPair[0], new Integer(ipPortPair[1]));
			hostArray[i] = host;
		}
		builder = RestClient.builder(hostArray);
		builder.setHttpClientConfigCallback(
				httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
		builder.setRequestConfigCallback(requestConfigBuilder->requestConfigBuilder.setConnectionRequestTimeout(5000).setSocketTimeout(60000)).setMaxRetryTimeoutMillis(60000);

		RestHighLevelClient restClient = new RestHighLevelClient(builder);
		
		EsTemplate template = new EsTemplate(restClient);
		
		TestData data = new TestData();
		data.setMessage("Test data for ES at"+System.currentTimeMillis());
		data.setPostDate("2019-05-21");
		data.setUser("Abhishek Aggarwal");
		template.indexDocument(data, "posts", "post", null);
	}
}