# ElasticSearch-Dao-Template
## *A generic way to perform CRUD operations with Elastic Search*

-----------------
POJO Design
-----------------

- Using this project one can perform CRUD operations from Java applications with Elastic Search.
- Entity pojo design structure should follow below convention:
  
  - Entity POJO MUST implement DocumentWrapper interface
  - Have a look @ class com.estemplate.models.TestData

```java
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
```
-------------------
TOOLS VERSIONS USED
-------------------
- JAVA: 1.8
- ELASTICSEARCH: 6.6.2
-----------
HOW TO USE?
-----------
- Build the maven project.
- Use the estemplate-0.0.1.jar in your projects as dependency.
- Instantiate EsTemplate class and use it for CRUD operations.

-----------------
TEST APPLICATION
-----------------
  - Update application.properties under resources as per your ES nodes
```
es.username=elastic
es.password = changeme
es.nodes = localhost:9200
```
  - Run _App.java_ to test document indexed:
  
 ----------------
EsTemplate APIs
----------------
Supports following operations:

public <T> ESResponse indexDocument(T t, String index, String mapping, String documentId)
public <T> T getFromIndex(Class<T> entityClass, String index, String mapping, String documentId)
public ESResponse isDocumentExists(String index, String mapping, String documentId)
public ESResponse deleteDocument(String index, String mapping, String documentId)

More methods to be added very soon.Stay tuned..

