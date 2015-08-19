package org.apache.chemistry.opencmis.couchbase;

import org.apache.chemistry.opencmis.commons.data.Properties;
import java.util.Map;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import org.apache.chemistry.opencmis.commons.data.PropertyData;


/**
 * Service to manage couchbase connections.
 * @author cecilelepape
 *
 */
public class CouchbaseService{
	
	static private CouchbaseService instance = null;
	private Cluster cluster = null;
	private Bucket bucket = null;
	
	static public CouchbaseService getInstance(){
		if(instance==null){
			instance = new CouchbaseService();
		}
		return instance;
	}
	
	public CouchbaseService(){
		cluster = CouchbaseCluster.create();
		bucket = cluster.openBucket("test");
		System.out.println("CouchbaseService : created. Cluster="+cluster+" - bucket="+bucket);
	}
	
	public void close(){
		if(cluster != null){
			cluster.disconnect();
		}
	}
	

	public JsonDocument createDocumentPropertiesOld(String docID, Properties docProperties){
		//Convert Java object to JSON format
		ObjectMapper mapper = new ObjectMapper();

		try {
			JsonObject doc = JsonObject.empty();

			
			Map<String, PropertyData<?>> map = docProperties.getProperties();
			System.out.println("nb of properties = "+map.size());
			int k = 0;
			PropertyData prop;
			for (Map.Entry<String, PropertyData<?>> entry : map.entrySet()){
				prop = entry.getValue();
				System.out.println("entry="+entry);
				//map.put(entry.getKey(), entry.getValue());
				doc.put(entry.getKey(),"value"+k);
				k++;
			}

			JsonDocument jsondoc = JsonDocument.create(docID, doc);
			JsonDocument response = bucket.upsert(jsondoc);
			System.out.println("Document properties created : docId="+docID+" - props="+docProperties);
			return response;
		}
		catch(Exception e){
			System.out.println("Cannot create document properties : docId="+docID+" - props="+docProperties);
			e.printStackTrace();
			return null;
		}
	}
	
	public JsonDocument createDocumentProperties(String docID, Properties docProperties){
		//Convert Java object to JSON format
		ObjectMapper mapper = new ObjectMapper();

		try {
			JsonObject doc = JsonObject.empty();
	//		String json = "";

/*			Map<String, String> map = new HashMap<String, String>();
			map.put("name", "mkyong");
			map.put("age", "29");
*/
			
			//PropertyData :   
			// String getId();
			// String getLocalName();
			// String getDisplayName();
			// String getQueryName();
			// List<T> getValues();
			// T getFirstValue();
			
			
			Map<String, PropertyData<?>> map = docProperties.getProperties();
			System.out.println("nb of properties = "+map.size());
			int k = 0;
			PropertyData prop;
			
			/*{
				  "id": "114",
				  "parentId": "@root@",
				  "cmis:lastModificationDate": {
				    "type": "datetime",
				    "displayName": "CMIS Last Modification Date Property",
				    "value": 1298396347157
				  },
				  "cmis:contentStreamFileName": {
				    "type": "string",
				    "displayName": "CMIS Content Stream File Name Property",
				    "value": "data1.txt"
				  },
				  "cmis:baseTypeId": {
				    "type": "id",
				    "displayName": "CMIS Base Type Id Property",
				    "value": "cmis:document"
				  },
				  "cmis:objectId": {
				    "type": "id",
				    "displayName": "CMIS Object Id Property",
				    "value": "114"
				  },
				  "cmis:createdBy": {
				    "type": "string",
				    "displayName": "CMIS Created By Property",
				    "value": "florian"
				  },
				  "cmis:creationDate": {
				    "type": "datetime",
				    "displayName": "CMIS Creation Date Property",
				    "value": 1298396347156
				  },
				  "cmis:name": {
				    "type": "string",
				    "displayName": "CMIS Name Property",
				    "value": "My_Document-1-0"
				  },
				  "customProp": {
				    "type": "string",
				    "displayName": "Sample Custom Property",
				    "value": "blue"
				  }
				*/
		
			// id
			doc.put("id", docID);
			
			// parentFolder
			doc.put("parentId","???");
			
			// "cmis:lastModificationDate":
			if(map.containsKey("cmis:lastModifiedBy")){
				prop = (PropertyData) map.get("cmis:lastModifiedBy");
				doc.put("cmis:lastModificationDate", prop.getFirstValue());
			}
			
			// cmis:objectTypeId
			if(map.containsKey("cmis:objectTypeId")){
				prop = (PropertyData) map.get("cmis:objectTypeId");
				doc.put("cmis:objectTypeId", prop.getFirstValue());
			}
		
			//cmis:createdBy
			if(map.containsKey("cmis:createdBy")){
				prop = (PropertyData) map.get("cmis:createdBy");
				doc.put("cmis:objectTypeId", prop.getFirstValue());
			}
			//
			
			//convert map to JSON string
	//		json = mapper.writeValueAsString(map);

	//Ò		System.out.println(json);

			
	/*	JsonObject doc = JsonObject.empty()
				.put("firstname", "Walter")
			    .put("lastname", "White")
			    .put("job", "chemistry teacher")
			    .put("age", 50);*/
		

			/*
			 * 
 */
			JsonDocument jsondoc = JsonDocument.create(docID, doc);
			JsonDocument response = bucket.upsert(jsondoc);
			System.out.println("Document properties created : docId="+docID+" - props="+docProperties);
			return response;
		}
		catch(Exception e){
			System.out.println("Cannot create document properties : docId="+docID+" - props="+docProperties);
			e.printStackTrace();
			return null;
		}
	}
}