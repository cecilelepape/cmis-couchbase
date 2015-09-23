package org.apache.chemistry.opencmis.couchbase;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;
import java.io.File;
import java.io.UnsupportedEncodingException;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;

import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisException;
import org.apache.chemistry.opencmis.commons.impl.json.JSONArray;
import org.omg.CORBA.TCKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CouchbaseService {
	private static final Logger LOG = LoggerFactory
			.getLogger(CouchbaseService.class);

	static private final String BUCKET = "test";

	static private final String CHILDREN = "cb:children";
	static public final String PATH_SEPARATOR = "/";

	private Cluster cluster = null;
	private Bucket bucket = null;

	static private CouchbaseService cbService = null;

	static public CouchbaseService getInstance() {
		if (cbService == null) {
			System.out.println("CouchbaseService created by thread "+Thread.currentThread().getName()+" - class:"+Thread.currentThread().getClass()+" - id:"+Thread.currentThread().getId()+" - state:"+Thread.currentThread().getState());
			
			cbService = new CouchbaseService();
		}
		return cbService;
	}

	private CouchbaseService() {
		cluster = CouchbaseCluster.create();
		bucket = cluster.openBucket(BUCKET);
		// creation of root node if not exist yet
		createRootFolderIfNotExists();
		debug("CouchbaseService started : bucket=" + bucket.name());
	}

	private boolean createRootFolderIfNotExists() {
		try {
			boolean rootExist = checkIfExists(CouchbaseRepository.ROOT_ID);
			if (!rootExist) {
				// create the root document in Couchbase
				JsonDocument doc = createFolderProperties(null, CouchbaseRepository.ROOT_ID, null);
				return doc!=null;
			}
			
			return true;
		} catch (CouchbaseException e) {
			debug("Root cannot be created !");
			return false;
		}
	}

	public void close() {
		if (cluster != null) {
			debug("CouchbaseService is stopping by thread "+Thread.currentThread().getName());
			Boolean isDisconnected = cluster.disconnect();
			debug("CouchbaseService stopped ? "+isDisconnected);
		}
	}

	public CmisObject getCmisObject(String objectId) {
		debug("CouchbaseService getCmisObject objectId:"
				+ objectId);
		if (this.bucket == null)
			return null;
		CmisObject data = new CmisObject(objectId);

		JsonDocument jsondoc = this.bucket.get(objectId);
		debug("jsondoc = " + jsondoc);
		if (jsondoc == null)
			return null;

		JsonObject doc = jsondoc.content();
		java.util.Set<java.lang.String> names = doc.getNames();

		for (String propId : names) {
			debug("propId = " + propId);

			// String Property
			if (PropertyIds.NAME.equals(propId)) {
				// debug("reading "+propId+" : "+doc.getString(propId));
				// objectInfo.setName(doc.getString(propId));
				data.setName(doc.getString(propId));
				data.setFileName(doc.getString(propId));
			} else if (PropertyIds.OBJECT_TYPE_ID.equals(propId)) {
				// debug("reading "+propId+" : "+doc.getString(propId));
				data.setType(doc.getString(propId));
			} else if (PropertyIds.CREATED_BY.equals(propId)) {
				// debug("reading "+propId+" : "+doc.getString(propId));
				data.setCreatedBy(doc.getString(propId));
			} else if (PropertyIds.LAST_MODIFIED_BY.equals(propId)) {
				// debug("reading "+propId+" : "+doc.getString(propId));
				data.setLastModifiedBy(doc.getString(propId));
			} else if (PropertyIds.CONTENT_STREAM_MIME_TYPE.equals(propId)) {
				// debug("reading "+propId+" : "+doc.getString(propId));
				data.setContentType(doc.getString(propId));
			} 
			else if (PropertyIds.CONTENT_STREAM_FILE_NAME.equals(propId)) {
				// debug("reading "+propId+" : "+doc.getString(propId));
				//data.setFileName(doc.getString(propId));
			}

			// calendar properties
			else if (PropertyIds.CREATION_DATE.equals(propId)) {
				// debug("reading "+propId+" : "+doc.getLong(propId));
				Long time = doc.getLong(propId);
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeInMillis(Long.valueOf(time));
				data.setCreationDate(cal);
				// debug("converted "+propId+" : "+cal);

			} else if (PropertyIds.LAST_MODIFICATION_DATE.equals(propId)) {
				// debug("reading "+propId+" : "+doc.getLong(propId));
				Long time = doc.getLong(propId);
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeInMillis(Long.valueOf(time));
				data.setLastModificationDate(cal);
				// debug("converted "+propId+" : "+cal);
			}

			else if (PropertyIds.PARENT_ID.equals(propId)) {
				// debug("reading"+propId+" : "+doc.getString(propId));
				// objectInfo.setLastModificationDate(doc.ge
				data.setParentId(doc.getString(propId));
			} else if (PropertyIds.OBJECT_ID.equals(propId)) {
				debug("Don't ovewrite property : " + propId);
				continue;
			}

			else if (PropertyIds.PATH.equals(propId)) {
				data.setPath(doc.getString(propId));
			}

			// list of children
			else if (CHILDREN.equals(propId)) {
				debug("CouchbaseService children ...");
				JsonArray jsa = doc.getArray(CHILDREN);
				int count = jsa.size();
				debug("CouchbaseService children count=" + count);

				for (int i = 0; i < count; i++) {
					debug("CouchbaseService children child[" + i
							+ "]=" + jsa.get(i));
					data.addChildren((String) jsa.get(i));
				}
			}
		}

		return data;

	}

	/**
	 * Create a folder within Couchbase.
	 * 
	 * @param parentData
	 *            le parent folder
	 * @param foldername
	 *            le name of the new folder
	 * @return the folder created
	 * @throws CouchbaseException
	 */
	public CmisObject createFolder(CmisObject parentData, String foldername,
			String username) throws CouchbaseException {

		debug("createFolder parentDataId:" + parentData.getId()
				+ " - folderName:" + foldername);
		try {
			// 0) folder id
			String folderId = getId(parentData, foldername);
			debug("FolderId will be = " + folderId);

			// 1) check if another folder with the same name doesn't exist
			List<String> folderList = parentData.getChildren();
			boolean alreadyExist = false;
			for (String child : folderList) {
				if (child != null && child.equals(folderId)) {
					debug("folder already exists : child=" + child);
					alreadyExist = true;
				}
			}

			if (alreadyExist) {
				throw new CouchbaseException(
						"Impossible to create this folder : it exists already.");
			}

			// 2) création du folder dans Couchbase
			JsonDocument result = createFolderProperties(parentData,
					foldername, username);
			if (result == null) {
				debug("Impossible to create folder properties in Couchbase");
				throw new CouchbaseException(
						"Impossible to create folder properties in Couchbase");
			}

			// 3) mise à jour des children de parentData et insertion dans
			// Couchbase
			parentData.addChildren(folderId);
			updateFolderProperties(parentData, username);

			return getCmisObject(folderId);

		} catch (Exception e) {
			debug("Impossible to create the new folder " + foldername);
			throw new CouchbaseException("Impossible to create the new folder "
					+ foldername);
		}

	}

	/*
	 * return the identifier of a folder or a document
	 */
	public String getId(CmisObject parentData, String objectname)
			throws UnsupportedEncodingException {
		String path = getPath(parentData);
		String fullpath = path + PATH_SEPARATOR + objectname;
		String folderId = Base64.encodeBytes(fullpath.getBytes("UTF-8"));
		return folderId;
	}

	// Path always ends with '/'
	public String getPath(CmisObject parentData) {
		return (parentData.isRoot() ? PATH_SEPARATOR : parentData.getPath()
				+ parentData.getName() + PATH_SEPARATOR);
	}

	public boolean checkIfExists(CmisObject parentData, String dataId)
			throws CouchbaseException {
		debug("checkifExists parentData" + parentData.getId() + " - docname="
				+ dataId);
		try {
			String objectId = getId(parentData, dataId);
			JsonDocument jsondoc = this.bucket.get(objectId);
			return jsondoc != null;
		} catch (UnsupportedEncodingException e) {
			throw new CouchbaseException("The name is incorrect : "
					+ e.getMessage());
		}
	}

	public boolean checkIfExists(String dataId) throws CouchbaseException {
		debug("checkifExists dataId" + dataId);
		JsonDocument jsondoc = this.bucket.get(dataId);
		return jsondoc != null;
	}

	public JsonDocument createFolderProperties(CmisObject parentData,
			String foldername, String username) {

		debug("createFolderProperties ...");
		try {
			JsonObject doc = JsonObject.empty();

			// id of the new folder
			String folderId = parentData == null ? CouchbaseRepository.ROOT_ID : getId(parentData, foldername);
			debug("folderId created : " + folderId);
			doc.put(PropertyIds.OBJECT_ID, folderId);

			// type of the new folder
			doc.put(PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());

			// name of the new folder
			doc.put(PropertyIds.NAME, foldername);

			// path of the new folder
			String path;
			if(parentData!=null) path = getPath(parentData);
			else path = "/";
			doc.put(PropertyIds.PATH, path);

			// id of the parent folder
			if(parentData!=null) doc.put(PropertyIds.PARENT_ID, parentData.getId());

			// last modified by
			doc.put(PropertyIds.LAST_MODIFIED_BY, username);

			// created by
			if(username!=null) doc.put(PropertyIds.CREATED_BY, username);

			// last modification date and creation date
			Long time = System.currentTimeMillis();
			// GregorianCalendar cal = new GregorianCalendar();
			// cal.setTimeInMillis(Long.valueOf(time));
			doc.put(PropertyIds.CREATION_DATE, time);
			doc.put(PropertyIds.LAST_MODIFICATION_DATE, time);

			// children empty
			doc.put(CHILDREN, new JSONArray());

			// store in couchbase
			JsonDocument jsondoc = JsonDocument.create(folderId, doc);
			JsonDocument response = bucket.upsert(jsondoc);

			debug("Folder properties created in Couchbase");
			return response;
		} catch (Exception e) {
			debug("Cannot create folder");
			e.printStackTrace();
			return null;
		}
	}

	public JsonDocument createDocumentProperties(CmisObject parentData,
			String docname, String username, ContentStream contentStream) {

		debug("createDocumentProperties ...");
		try {
			JsonObject doc = JsonObject.empty();

			// id of the new document
			String docId = getId(parentData, docname);
			debug("docId created : " + docId);
			doc.put(PropertyIds.OBJECT_ID, docId);

			// type of the new document
			doc.put(PropertyIds.OBJECT_TYPE_ID,
					BaseTypeId.CMIS_DOCUMENT.value());

			// name of the new document
			doc.put(PropertyIds.NAME, docname);

			// path of the new folder
			String path = getPath(parentData);
			doc.put(PropertyIds.PATH, path);

			// id of the parent folder
			doc.put(PropertyIds.PARENT_ID, parentData.getId());

			// last modified by
			doc.put(PropertyIds.LAST_MODIFIED_BY, username);

			// created by
			doc.put(PropertyIds.CREATED_BY, username);

			// last modification date and creation date
			Long time = System.currentTimeMillis();
			// GregorianCalendar cal = new GregorianCalendar();
			// cal.setTimeInMillis(Long.valueOf(time));
			doc.put(PropertyIds.CREATION_DATE, time);
			doc.put(PropertyIds.LAST_MODIFICATION_DATE, time);

			// content
			doc.put(PropertyIds.CONTENT_STREAM_FILE_NAME, docname);
			doc.put(PropertyIds.CONTENT_STREAM_MIME_TYPE,
					contentStream.getMimeType());
			doc.put(PropertyIds.CONTENT_STREAM_LENGTH,
					contentStream.getLength());

			// store in couchbase
			JsonDocument jsondoc = JsonDocument.create(docId, doc);
			JsonDocument response = bucket.upsert(jsondoc);

			debug("Folder properties created in Couchbase");
			return response;
		} catch (Exception e) {
			debug("Cannot create folder");
			e.printStackTrace();
			return null;
		}
	}

	public JsonDocument updateFolderProperties(CmisObject folderData,
			String username) {
		debug("updateFolderProperties folderData=" + folderData);
		try {
			JsonObject doc = JsonObject.empty();

			/*
			 * "cmis:objectId": "xxxx", "cmis:objectTypeId":"cmis:folder",
			 * "cmis:name": "folder1", "cmis:path": "/", "cmis:parent":"@root@"
			 * "cmis:lastModifiedBy": "test", "cmis:createdBy": "test",
			 * "cmis:creationDate":1441200150000, "cmis:lastModificationDate":
			 * 1441200150000, "cb:children": []
			 */

			// id of the new folder
			doc.put(PropertyIds.OBJECT_ID, folderData.getId());

			// type of the new folder
			doc.put(PropertyIds.OBJECT_TYPE_ID, folderData.getType());

			// name of the new folder
			doc.put(PropertyIds.NAME, folderData.getName());

			// path of the new folder
			doc.put(PropertyIds.PATH, folderData.getPath());

			// id of the parent folder
			if (folderData.getParentId() != null)
				doc.put(PropertyIds.PARENT_ID, folderData.getParentId());

			// last modified by
			doc.put(PropertyIds.LAST_MODIFIED_BY, username);

			// created by
			doc.put(PropertyIds.CREATED_BY, folderData.getCreatedBy());

			// creation date
			doc.put(PropertyIds.CREATION_DATE, folderData.getCreationDate()
					.getTimeInMillis());

			// last modification date
			Long time = System.currentTimeMillis();
			// GregorianCalendar cal = new GregorianCalendar();
			// cal.setTimeInMillis(Long.valueOf(time));
			doc.put(PropertyIds.LAST_MODIFICATION_DATE, time);

			// children
			debug("storing children ...");
			JsonArray children = JsonArray.from(folderData.getChildren());
			doc.put(CHILDREN, children);
			debug("storing children ok.");

			// store in couchbase
			String docId = (String) doc.get(PropertyIds.OBJECT_ID);
			JsonDocument jsondoc = JsonDocument.create(docId, doc);
			debug("Folder properties created in Couchbase ...");

			JsonDocument response = bucket.upsert(jsondoc);

			debug("Folder properties created in Couchbase done.");
			return response;
		} catch (Exception e) {
			debug("Cannot create folder");
			e.printStackTrace();
			return null;
		}
	}

	public CmisObject createDocument(CmisObject parentData,
			String documentname, String username, ContentStream contentStream)
			throws CouchbaseException {
		debug("createNewDocument not yet implemented");

		try {
			// 0) document id
			String docId = getId(parentData, documentname);
			debug("docId will be = " + docId);

			// 1) check if another document with the same name doesn't exist
			// in the same folder
			List<String> folderList = parentData.getChildren();
			boolean alreadyExist = false;
			for (String child : folderList) {
				if (child != null && child.equals(docId)) {
					debug("document or folder already exists : child=" + child);
					alreadyExist = true;
				}
			}

			if (alreadyExist) {
				throw new CouchbaseException(
						"Impossible to create this document : it exists already.");
			}

			// 2) creation of the document in Couchbase
			JsonDocument result = createDocumentProperties(parentData,
					documentname, username, contentStream);
			if (result == null) {
				debug("Impossible to create document properties in Couchbase");
				throw new CouchbaseException(
						"Impossible to create document properties in Couchbase");
			}

			// 3) update children of parentData and insertion in
			// Couchbase
			parentData.addChildren(docId);
			updateFolderProperties(parentData, username);

			return getCmisObject(docId);

		} catch (Exception e) {
			debug("Impossible to create the new folder " + documentname);
			throw new CouchbaseException(
					"Impossible to create the new document " + documentname);
		}
	}

	public boolean deleteProperties(CmisObject data, String username)
			throws CouchbaseException {
		if (data == null)
			throw new CouchbaseException("Properties unknown");

		if (data.isRoot())
			throw new CouchbaseException("Cannot delete root");

		// remove the data (file or document) from its parent folder
		CmisObject parentData = getCmisObject(data.getParentId());
		if (parentData == null)
			throw new CouchbaseException("Parent is not set");

		// 1) check if another document with the same name doesn't exist
		// in the same folder
		List<String> folderList = parentData.getChildren();
		boolean alreadyExist = false;
		for (String child : folderList) {
			if (child != null && child.equals(data.getId())) {
				debug("document or folder already exists : child=" + child);
				alreadyExist = true;
			}
		}

		if (!alreadyExist)
			throw new CouchbaseException("data does not belong to its parent");

		// 3) update children of parentData and insertion in
		// Couchbase
		boolean removedFromParent = parentData.removeChildren(data.getId());
		if (!removedFromParent)
			throw new CouchbaseException(
					"cannot remove data from parent properties");

		// update last modification date
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(Long.valueOf(System.currentTimeMillis()));
		parentData.setLastModificationDate(cal);

		// update last user
		parentData.setLastModifiedBy(username);

		JsonDocument newParentDoc = updateFolderProperties(parentData, username);
		if (newParentDoc == null)
			throw new CouchbaseException("cannot update parent properties");

		// remove the data itself
		JsonDocument doc = bucket.remove(data.getId());
		return doc != null;
	}

	public void readProperties(String objectId, PropertiesImpl properties,
			Set<String> filter, ObjectInfoImpl objectInfo) throws Exception {
		debug("=======readProperties objectId = " + objectId);
		// get the json from couchbase
		if (this.bucket == null)
			return;
		JsonDocument jsondoc = this.bucket.get(objectId);
		debug("jsondoc = " + jsondoc);
		if (jsondoc == null)
			throw new Exception("Document does not exist  - docId : "
					+ objectId);

		JsonObject doc = jsondoc.content();
		java.util.Set<java.lang.String> names = doc.getNames();

		PropertyData<?> prop = null;

		for (String propId : names) {
			debug("propId = " + propId);

			// String Property
			if (PropertyIds.NAME.equals(propId)) {
				// debug("reading "+propId+" : "+doc.getString(propId));
				objectInfo.setName(doc.getString(propId));

				prop = new PropertyStringImpl(propId, doc.getString(propId));
			} else if (PropertyIds.OBJECT_TYPE_ID.equals(propId)) {
				// debug("reading "+propId+" : "+doc.getString(propId));
				objectInfo.setTypeId(doc.getString(propId));

				prop = new PropertyStringImpl(propId, doc.getString(propId));
			} else if (PropertyIds.CREATED_BY.equals(propId)) {
				// debug("reading "+propId+" : "+doc.getString(propId));
				objectInfo.setCreatedBy(doc.getString(propId));

				prop = new PropertyStringImpl(propId, doc.getString(propId));
			} else if (PropertyIds.CONTENT_STREAM_MIME_TYPE.equals(propId)) {
				// debug("reading "+propId+" : "+doc.getString(propId));
				objectInfo.setContentType(doc.getString(propId));

				prop = new PropertyStringImpl(propId, doc.getString(propId));
			} else if (PropertyIds.CONTENT_STREAM_FILE_NAME.equals(propId)) {
				// debug("reading "+propId+" : "+doc.getString(propId));
				objectInfo.setFileName(doc.getString(propId));

				prop = new PropertyStringImpl(propId, doc.getString(propId));
			}

			// calendar properties
			else if (PropertyIds.CREATION_DATE.equals(propId)) {
				// debug("reading "+propId+" : "+doc.getLong(propId));
				Long time = doc.getLong(propId);
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeInMillis(Long.valueOf(time));
				objectInfo.setCreationDate(cal);
				// debug("converted "+propId+" : "+cal);

				prop = new PropertyDateTimeImpl(propId, cal);
			} else if (PropertyIds.LAST_MODIFICATION_DATE.equals(propId)) {
				// debug("reading "+propId+" : "+doc.getLong(propId));
				Long time = doc.getLong(propId);
				GregorianCalendar cal = new GregorianCalendar();
				cal.setTimeInMillis(Long.valueOf(time));
				objectInfo.setLastModificationDate(cal);
				// debug("converted "+propId+" : "+cal);

				prop = new PropertyDateTimeImpl(propId, cal);
			}

			// properties previously read from the file system but now stored in
			// couchbase
			else if (PropertyIds.LAST_MODIFIED_BY.equals(propId)) {
				// debug("reading"+propId+" : "+doc.getString(propId));
				// objectInfo.setLastModificationDate(doc.getString(propId));

				prop = new PropertyStringImpl(propId, doc.getString(propId));
			} else if (PropertyIds.PARENT_ID.equals(propId)) {
				// debug("reading"+propId+" : "+doc.getString(propId));
				// objectInfo.setLastModificationDate(doc.getString(propId));

				prop = new PropertyStringImpl(propId, doc.getString(propId));
			}

			// check filter
			if (filter != null) {
				if (!filter.contains(prop.getQueryName())) {
					continue;
				} else {
					filter.remove(prop.getQueryName());
				}
			}

			// don't overwrite id
			if (PropertyIds.OBJECT_ID.equals(propId)) {
				debug("Don't ovewrite property : " + propId);
				continue;
			}

			// don't overwrite base type
			if (PropertyIds.BASE_TYPE_ID.equals(propId)) {
				debug("Don't ovewrite property : " + propId);
				continue;
			}

			debug("Replacing property : " + prop);
			properties.replaceProperty(prop);
		}

		debug("=======readProperties done======");
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (cluster == null)
			return "cluster is null";
		buf.append("cluster:" + cluster + "\n");

		if (bucket == null)
			buf.append("bucket is null");
		buf.append("bucket:" + bucket);
		return buf.toString();
	}

	private void debug(String msg) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("[CouchbaseService] {}", msg);
		}
		//System.out.println("{" + msg + "}");
	}
}
