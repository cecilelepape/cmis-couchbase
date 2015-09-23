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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestStorage {

	static String storagePath = "/Users/cecilelepape/Documents/CMIS/repo/couchbase";
	static String tempPath = "/Users/cecilelepape/Documents/tools/apache-tomcat-8.0-2.24/temp";
	
	static final String repoId = "test";
	static CouchbaseService cbService = null;
	static LocalStorageService localStorageService = null;

	public TestStorage() {
	}

	
	@BeforeClass
	static public void before(){
		// recupérer les propriétés de repository.properties
		
	}
	
	@AfterClass
	static public void after(){
		
		if(localStorageService != null){
			localStorageService.close();
		}
	}

	@Test
	public void testInitStorage() {
	
	}

}
