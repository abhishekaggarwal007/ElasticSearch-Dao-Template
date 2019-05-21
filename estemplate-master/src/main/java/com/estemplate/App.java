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
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;

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
		data.setTrain("Rajdhani");
		data.setPostDate("2019-05-21");
		data.setStatus("Cancelled");
		template.indexDocument(data, "posts", "post", null);
		
		TestData data1 = new TestData();
		data1.setTrain("Rajdhani");
		data1.setPostDate("2019-05-22");
		data1.setStatus("Completed");
		template.indexDocument(data1, "posts", "post", null);
		
		TermsAggregationBuilder parentAggregation = AggregationBuilders.terms("by_train").field("train");
		parentAggregation.subAggregation(AggregationBuilders.terms("by_status").field("status"));
		parentAggregation.subAggregation(AggregationBuilders.topHits("by_top_train").size(1));
		parentAggregation.subAggregation(AggregationBuilders.count("by_count").field("postDate"));
		
		SearchResponse response = template.querywithAggregation(null, parentAggregation, 0, 0, "posts");
	}
}