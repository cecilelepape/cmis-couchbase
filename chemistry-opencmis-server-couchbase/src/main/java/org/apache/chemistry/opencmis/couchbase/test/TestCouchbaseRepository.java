package org.apache.chemistry.opencmis.couchbase.test;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyId;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfo;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.couchbase.CmisObject;
import org.apache.chemistry.opencmis.couchbase.CouchbaseException;
import org.apache.chemistry.opencmis.couchbase.CouchbaseRepository;
import org.apache.chemistry.opencmis.couchbase.CouchbaseRepositoryManager;
import org.apache.chemistry.opencmis.couchbase.CouchbaseService;
import org.apache.chemistry.opencmis.couchbase.CouchbaseTypeManager;
import org.apache.chemistry.opencmis.couchbase.LocalStorageService;
import org.apache.chemistry.opencmis.couchbase.StorageService;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestCouchbaseRepository {

	static String storagePath = "/Users/cecilelepape/Documents/CMIS/repo/couchbase";
	static String tempPath = "/Users/cecilelepape/Documents/tools/apache-tomcat-8.0-2.24/temp";
	
	CouchbaseRepository repo = null;
	CouchbaseRepositoryManager repoManager = null;
	CouchbaseTypeManager typeManager = null;
	static final String repoId = "test";
	static CouchbaseService cbService = null;
	static StorageService storageService = null;

	public TestCouchbaseRepository() {
		repoManager = new CouchbaseRepositoryManager();
		typeManager = new CouchbaseTypeManager();
	}

	
	@BeforeClass
	static public void before(){
		cbService = CouchbaseService.getInstance();
		storageService = new LocalStorageService(storagePath);
	}
	
	@AfterClass
	static public void after(){
		if(cbService !=null){
			cbService.close();
			cbService = null;
		}
		if(storageService != null){
			storageService.close();
			storageService = null;
		}
	}

	@Test
	public void testGetRepo() {
		repo = new CouchbaseRepository(repoId,
				typeManager);
		assertNotNull("repo not found", repo);
		repo.setStorageService(storageService);
		// check if root document exists
		try {
			boolean rootExists = cbService.checkIfExists(CouchbaseRepository.ROOT_ID);
			assertTrue("Root document does not exist in Couchbase", rootExists);
			System.out.println("Root document exists.");
		} catch (CouchbaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
	@Test
	public void testCreateFolderAndDocuments() {

		try {
			repo = new CouchbaseRepository(repoId,
					typeManager);
			assertNotNull("repo not found", repo);
			System.out.println("Repository ok.");

			repo.setUserReadWrite("test");

			CallContext context = createFakeContext();
			// get root folder

			ObjectInfoHandler infoHandlerA = createObjectInfoHandler();
			ObjectData root = repo.getObject(context, CouchbaseRepository.ROOT_ID, null, null,
					null, null, infoHandlerA);

			assertNotNull("root is null", root);
			
			CmisObject rootObj = cbService.getCmisObject(CouchbaseRepository.ROOT_ID);
			assertNotNull("root is stored in CB",rootObj);

			// add a subfolder folderA
			Properties propFolderA = createFakeFolderProperties("folderA");
			String folderA = repo.createFolder(context, propFolderA, CouchbaseRepository.ROOT_ID);
			assertNotNull("folder cannot be created", folderA);

			assertTrue("folderA is not stored in cb", cbService.checkIfExists(rootObj, "folderA"));
			System.out.println("FolderA created with id :" + folderA);

			// add a document docA to folderA
			Properties propDocB = createFakeDocumentProperties("doc1.txt", folderA);
			ObjectInfoHandler infoHandlerB = createObjectInfoHandler();
			ContentStream cStream = new ContentStreamImpl("Doc1.txt", "text/plain", "This is a test file called doc1.txt and stored in folderA");
			ObjectData docDataB = repo.create(context, propDocB, folderA, cStream, null, infoHandlerB);
			assertNotNull("doc1.txt cannot be created", docDataB);
			System.out.println("doc1.txt created");
			assertTrue("doc1.txt is not stored in couchbase", cbService.checkIfExists(docDataB.getId()));
			System.out.println("doc1.txt stored in couchbase with id:"+docDataB.getId());
			assertTrue("doc1.txt is not stored in the storage area", storageService.exists(docDataB.getId()));
			System.out.println("doc1.txt stored in storage area");

			// retrieve content from folderA
			
			// remove content from folderA
			repo.deleteObject(context, docDataB.getId());
			assertTrue("doc1.txt cannot be removed from cb", !cbService.checkIfExists(docDataB.getId()));
			System.out.println("doc1.txt removed from cb");
			assertTrue("doc1.txt cannot be removed from storage area", !storageService.exists(docDataB.getId()));
			System.out.println("doc1.txt removed from storage area");
			
			// remove folderA
			repo.deleteObject(context, folderA);
			assertTrue("folderA cannot be removed from cb", !cbService.checkIfExists(folderA));
			System.out.println("FolderA removed");

			
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception : " + e.getMessage());
		} 
	}

	

	private Properties createFakeFolderProperties(String folderName) {

		Collection<PropertyData<?>> propList = new ArrayList<PropertyData<?>>();

		PropertyId propTypeId = new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID,
				"cmis:folder");
		propList.add(propTypeId);

		PropertyString propName = new PropertyStringImpl(PropertyIds.NAME, folderName);
		propList.add(propName);

		Properties properties = new PropertiesImpl(propList);
		return properties;
	}
	private Properties createFakeDocumentProperties(String docName, String folderId) {

		Collection<PropertyData<?>> propList = new ArrayList<PropertyData<?>>();

		PropertyId propTypeId = new PropertyIdImpl(PropertyIds.OBJECT_TYPE_ID,
				"cmis:document");
		propList.add(propTypeId);

		PropertyString propName = new PropertyStringImpl(PropertyIds.NAME, docName);
		propList.add(propName);
		
		/*PropertyString parentName = new PropertyStringImpl(PropertyIds.PARENT_ID, folderId);
		propList.add(parentName);*/
		

		Properties properties = new PropertiesImpl(propList);
		return properties;
	}
	
	private ObjectInfoHandler createObjectInfoHandler() {
		ObjectInfoHandler handler = new ObjectInfoHandler() {
			HashMap<String, ObjectInfo> infoMap = new HashMap<String, ObjectInfo>();

			@Override
			public ObjectInfo getObjectInfo(String repositoryId, String objectId) {
				return infoMap.get(objectId);
			}

			@Override
			public void addObjectInfo(ObjectInfo objectInfo) {
				infoMap.put(objectInfo.getId(), objectInfo);
			}
		};
		return handler;
	}
	
	private CallContext createFakeContext() {

		CallContext context = new CallContext() {

			@Override
			public boolean isObjectInfoRequired() {
				return true;
			}

			@Override
			public String getUsername() {
				return "test";
			}

			@Override
			public File getTempDirectory() {
				return new File(tempPath);
			}

			@Override
			public String getRepositoryId() {
				return "test";
			}

			@Override
			public String getPassword() {
				return "test";
			}

			@Override
			public BigInteger getOffset() {
				return null;
			}

			@Override
			public int getMemoryThreshold() {
				return 4194304;
			}

			@Override
			public long getMaxContentSize() {
				return 4294967296l;
			}

			@Override
			public String getLocale() {
				return null;
			}

			@Override
			public BigInteger getLength() {
				return null;
			}

			@Override
			public CmisVersion getCmisVersion() {
				return CmisVersion.CMIS_1_1;
			}

			@Override
			public String getBinding() {
				return "atompub";
			}

			@Override
			public Object get(String key) {
				return null;
			}

			@Override
			public boolean encryptTempFiles() {
				return false;
			}
		};

		return context;
	}
}
