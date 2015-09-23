/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.chemistry.opencmis.couchbase;

import java.util.Map;

import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;

/**
 * Manages all repositories.
 */
public class StorageFactory {

	static final String STORAGE = "storage";
	static final String PATH = "path";
	static final String LOCAL = "local";
	static final String AWS = "aws";
	static final String BUCKET = "bucket";
	static final String FOLDER = "folder";

	
	static public StorageService createStorageService(Map<String,String> parameters) throws CmisConstraintException{
		System.out.println("===STORAGE FACTORY===");
		for(String paramkey : parameters.keySet()){
			System.out.println("key="+paramkey+" - value="+parameters.get(paramkey));
		}
		System.out.println("=================");
		
		String storageType = parameters.get(STORAGE);
		if(LOCAL.equals(storageType)) return createLocalStorageService(parameters);
		if(AWS.equals(storageType)) return createAWSStorageService(parameters);
		
		throw new CmisConstraintException("Storage undefined : type="+storageType);
	}
	
	static private StorageService createLocalStorageService(Map<String,String> parameters) throws CmisConstraintException{
		String storageType = parameters.get(STORAGE);
		if(! LOCAL.equals(storageType)) return null;
		String rootpath = parameters.get(PATH);
		if(rootpath==null) throw new CmisConstraintException("Local storage path is undefined");
		return new LocalStorageService(rootpath);
	}
	
	static private StorageService createAWSStorageService(Map<String,String> parameters) throws CmisConstraintException{
		String storageType = parameters.get(STORAGE);
		if(! AWS.equals(storageType)) return null;
		String bucket = parameters.get(BUCKET);
		if(bucket==null) throw new CmisConstraintException("AWS bucket is undefined");
		String folder = parameters.get(FOLDER);
		if(folder==null) throw new CmisConstraintException("AWS folder is undefined");
		
		return new AWSStorageService(bucket, folder);
	}
	
	
}
