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

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.chemistry.opencmis.commons.impl.server.AbstractServiceFactory;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.server.support.wrapper.CallContextAwareCmisService;
import org.apache.chemistry.opencmis.server.support.wrapper.CmisServiceWrapperManager;
import org.apache.chemistry.opencmis.server.support.wrapper.ConformanceCmisServiceWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.internal.schedulers.NewThreadWorker;
import rx.schedulers.Schedulers;

/**
 * couchbase Service Factory.
 */
public class CouchbaseCmisServiceFactory extends AbstractServiceFactory {

	private static final Logger LOG = LoggerFactory
			.getLogger(CouchbaseCmisServiceFactory.class);

	private static final String PREFIX_LOGIN = "login.";
	private static final String PREFIX_REPOSITORY = "repository.";
	private static final String PREFIX_TYPE = "type.";
	private static final String SUFFIX_READWRITE = ".readwrite";
	private static final String SUFFIX_READONLY = ".readonly";
	private static final String SUFFIX_STORAGE = ".storage";

	/** Default maxItems value for getTypeChildren()}. */
	private static final BigInteger DEFAULT_MAX_ITEMS_TYPES = BigInteger
			.valueOf(50);

	/** Default depth value for getTypeDescendants(). */
	private static final BigInteger DEFAULT_DEPTH_TYPES = BigInteger
			.valueOf(-1);

	/**
	 * Default maxItems value for getChildren() and other methods returning
	 * lists of objects.
	 */
	private static final BigInteger DEFAULT_MAX_ITEMS_OBJECTS = BigInteger
			.valueOf(200);

	/** Default depth value for getDescendants(). */
	private static final BigInteger DEFAULT_DEPTH_OBJECTS = BigInteger
			.valueOf(10);

	/** Each thread gets its own {@link CouchbaseCmisService} instance. */
	private ThreadLocal<CallContextAwareCmisService> threadLocalService = new ThreadLocal<CallContextAwareCmisService>();

	/** The couchbase service */
	CouchbaseService couchbaseService = null;

	private CouchbaseRepositoryManager repositoryManager;
	private CouchbaseUserManager userManager;
	private CouchbaseTypeManager typeManager;
	private CmisServiceWrapperManager wrapperManager;

	public CouchbaseRepositoryManager getRepositoryManager() {
		return repositoryManager;
	}

	public CouchbaseUserManager getUserManager() {
		return userManager;
	}

	public CouchbaseTypeManager getTypeManager() {
		return typeManager;
	}

	@Override
	public void init(Map<String, String> parameters) {
		repositoryManager = new CouchbaseRepositoryManager();
		userManager = new CouchbaseUserManager();
		typeManager = new CouchbaseTypeManager();

		wrapperManager = new CmisServiceWrapperManager();
		wrapperManager.addWrappersFromServiceFactoryParameters(parameters);
		wrapperManager.addOuterWrapper(ConformanceCmisServiceWrapper.class,
				DEFAULT_MAX_ITEMS_TYPES, DEFAULT_DEPTH_TYPES,
				DEFAULT_MAX_ITEMS_OBJECTS, DEFAULT_DEPTH_OBJECTS);

		couchbaseService = CouchbaseService.getInstance();
		readConfiguration(parameters);
	}

	@Override
	public void destroy() {
		System.out
				.println("<CouchbaseCmisServiceFactory> destroy called by thread "
						+ Thread.currentThread().getName()
						+ " - class:"
						+ Thread.currentThread().getClass()
						+ " - id:"
						+ Thread.currentThread().getId()
						+ " - state:"
						+ Thread.currentThread().getState());

		if (couchbaseService != null)
			couchbaseService.close();
		couchbaseService = null;
		forceRxJavaSchedulerShutdown();

		threadLocalService = null;
	}

	private void forceRxJavaSchedulerShutdown() {
		LOG.warn("FORCE RxJava Scheduler shutdown : Schedulers.computation="
				+ Schedulers.computation());

		Object pool = getField(Schedulers.computation(), "pool");
		if (pool != null) {
			for (NewThreadWorker w : (NewThreadWorker[]) getField(pool,
					"eventLoops")) {
				w.unsubscribe();
			}
		} else {
			System.out.println("no pool found");
		}
	}

	private Object getField(Object object, String field) {
		try {
			Class<? extends Object> clazz = object.getClass();
			Field[] fields = clazz.getFields();

			LOG.warn("Found " + fields.length + " fields");
			for (Field iField : fields) {
				LOG.warn("Found Field: " + iField.toString() + " - "
						+ iField.getName());
			}

			if (Arrays.asList().contains(field))
				return clazz.getField(field);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public CmisService getService(CallContext context) {
		// authenticate the user
		// if the authentication fails, authenticate() throws a
		// CmisPermissionDeniedException
		userManager.authenticate(context);

		// get service object for this thread
		CallContextAwareCmisService service = threadLocalService.get();
		if (service == null) {
			// there is no service object for this thread -> create one
			CouchbaseCmisService couchbaseService = new CouchbaseCmisService(
					repositoryManager);

			service = (CallContextAwareCmisService) wrapperManager
					.wrap(couchbaseService);

			threadLocalService.set(service);
		}

		// hand over the call context to the service object
		service.setCallContext(context);

		return service;
	}

	// ---- helpers ----

	/**
	 * Reads the configuration and sets up the repositories, logins, and type
	 * definitions.
	 */
	private void readConfiguration(Map<String, String> parameters) {
		List<String> keys = new ArrayList<String>(parameters.keySet());
		Collections.sort(keys);

		for (String key : keys) {
			System.out.println("Config key=" + key);
			if (key.startsWith(PREFIX_LOGIN)) {
				// get logins
				String usernameAndPassword = replaceSystemProperties(parameters
						.get(key));
				if (usernameAndPassword == null) {
					continue;
				}

				String username = usernameAndPassword;
				String password = "";

				int x = usernameAndPassword.indexOf(':');
				if (x > -1) {
					username = usernameAndPassword.substring(0, x);
					password = usernameAndPassword.substring(x + 1);
				}

				LOG.info("Adding login '{}'.", username);

				userManager.addLogin(username, password);
			} else if (key.startsWith(PREFIX_TYPE)) {
				// load type definition
				String typeFile = replaceSystemProperties(parameters.get(key)
						.trim());
				if (typeFile.length() == 0) {
					continue;
				}

				LOG.info("Loading type definition: {}", typeFile);

				if (typeFile.charAt(0) == '/') {
					try {
						typeManager.loadTypeDefinitionFromResource(typeFile);
						continue;
					} catch (IllegalArgumentException e) {
						// resource not found -> try it as a regular file
					} catch (Exception e) {
						LOG.warn(
								"Could not load type defintion from resource '{}': {}",
								typeFile, e.getMessage(), e);
						continue;
					}
				}

				try {
					typeManager.loadTypeDefinitionFromFile(typeFile);
				} catch (Exception e) {
					LOG.warn(
							"Could not load type defintion from file '{}': {}",
							typeFile, e.getMessage(), e);
				}
			} else if (key.startsWith(PREFIX_REPOSITORY)) {
				System.out.println("config starts with " + PREFIX_REPOSITORY);
				// configure repositories
				String repositoryId = key.substring(PREFIX_REPOSITORY.length())
						.trim();
				int x = repositoryId.lastIndexOf('.');
				if (x > 0) {
					repositoryId = repositoryId.substring(0, x);
				}

				if (repositoryId.length() == 0) {
					throw new IllegalArgumentException("No repository id!");
				}

				if (key.endsWith(SUFFIX_READWRITE)) {
					System.out.println("config ends with " + SUFFIX_READWRITE);

					// read-write users
					CouchbaseRepository cbr = repositoryManager
							.getRepository(repositoryId);
					for (String user : split(parameters.get(key))) {
						cbr.setUserReadWrite(replaceSystemProperties(user));
					}
				} else if (key.endsWith(SUFFIX_READONLY)) {
					System.out.println("config ends with " + SUFFIX_READONLY);

					// read-only users
					CouchbaseRepository cbr = repositoryManager
							.getRepository(repositoryId);
					for (String user : split(parameters.get(key))) {
						cbr.setUserReadOnly(replaceSystemProperties(user));
					}
				} else if (key.endsWith(SUFFIX_STORAGE)) {
					System.out.println("Config ends with " + SUFFIX_STORAGE);
					CouchbaseRepository cbr = repositoryManager
							.getRepository(repositoryId);
					String storagetype = parameters.get(key);
					System.out.println("Config storage type " + storagetype);

		
					Map<String, String> storageMap = new HashMap<String, String>();
					storageMap.put("storage", storagetype);

					String prefix = PREFIX_REPOSITORY + repositoryId
							+ SUFFIX_STORAGE+".";

					for (String k : parameters.keySet()) {
						System.out.println(k + " starts with " + prefix + " : "
								+ k.startsWith(prefix));
						if (k.startsWith(prefix)) {
							String newkey = k.substring(prefix.length());
							System.out.println("newkey=" + newkey);
							storageMap.put(newkey, parameters.get(k));
						}
					}
					StorageService storageService = StorageFactory
							.createStorageService(storageMap);
					cbr.setStorageService(storageService);

				} else if((PREFIX_REPOSITORY + repositoryId).equals(key)) {
					System.out.println("Config default : creation of repo "
							+ repositoryId);

					// new repository
					// String root =
					// replaceSystemProperties(parameters.get(key));

					LOG.info("Adding repository '{}'", repositoryId);

					// CouchbaseRepository cbr = new
					// CouchbaseRepository(repositoryId, root, typeManager);
					CouchbaseRepository cbr = new CouchbaseRepository(
							repositoryId, typeManager);

					repositoryManager.addRepository(cbr);
				}
			}
		}
	}

	/**
	 * Splits a string by comma.
	 */
	private List<String> split(String csl) {
		if (csl == null) {
			return Collections.emptyList();
		}

		List<String> result = new ArrayList<String>();
		for (String s : csl.split(",")) {
			result.add(s.trim());
		}

		return result;
	}

	/**
	 * Finds all substrings in curly braces and replaces them with the value of
	 * the corresponding system property.
	 */
	private String replaceSystemProperties(String s) {
		if (s == null) {
			return null;
		}

		StringBuilder result = new StringBuilder(128);
		StringBuilder property = null;
		boolean inProperty = false;

		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);

			if (inProperty) {
				if (c == '}') {
					String value = System.getProperty(property.toString());
					if (value != null) {
						result.append(value);
					}
					inProperty = false;
				} else {
					property.append(c);
				}
			} else {
				if (c == '{') {
					property = new StringBuilder(32);
					inProperty = true;
				} else {
					result.append(c);
				}
			}
		}

		return result.toString();
	}

}
