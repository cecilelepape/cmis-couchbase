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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.apache.chemistry.opencmis.commons.BasicPermissions;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.BulkUpdateObjectIdAndChangeToken;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.PermissionMapping;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.PropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyDateTime;
import org.apache.chemistry.opencmis.commons.data.PropertyString;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.PermissionDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityAcl;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityContentStreamUpdates;
import org.apache.chemistry.opencmis.commons.enums.CapabilityJoin;
import org.apache.chemistry.opencmis.commons.enums.CapabilityOrderBy;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.CapabilityRenditions;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.enums.SupportedPermissions;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.impl.Base64;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.commons.impl.XMLConstants;
import org.apache.chemistry.opencmis.commons.impl.XMLConverter;
import org.apache.chemistry.opencmis.commons.impl.XMLUtils;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlEntryImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AccessControlPrincipalDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AclCapabilitiesDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.AllowableActionsImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.BulkUpdateObjectIdAndChangeTokenImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.CreatablePropertyTypesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FailedToDeleteDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.NewTypeSettableAttributesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderListImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectParentDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PartialContentStreamImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionDefinitionDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PermissionMappingDataImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyBooleanImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDateTimeImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyDecimalImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyHtmlImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIdImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyIntegerImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyStringImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.PropertyUriImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryCapabilitiesImpl;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.RepositoryInfoImpl;
import org.apache.chemistry.opencmis.commons.impl.server.ObjectInfoImpl;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.ObjectInfoHandler;
import org.apache.chemistry.opencmis.commons.spi.Holder;
import org.apache.chemistry.opencmis.server.impl.ServerVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements all repository operations.
 */
public class CouchbaseRepository {

	private static final Logger LOG = LoggerFactory
			.getLogger(CouchbaseRepository.class);

	public static final String ROOT_ID = "@root@";
	private static final String SHADOW_EXT = ".cmis.xml";
	private static final String SHADOW_FOLDER = "cmis.xml";

	private static final int BUFFER_SIZE = 64 * 1024;

	/** Repository id. */
	private final String repositoryId;
	/** Root directory. */
	// private final File root;
	/** Types. */
	private final CouchbaseTypeManager typeManager;
	/** Users. */
	private final Map<String, Boolean> readWriteUserMap;

	/** CMIS 1.0 repository info. */
	private final RepositoryInfo repositoryInfo10;
	/** CMIS 1.1 repository info. */
	private final RepositoryInfo repositoryInfo11;

	private CouchbaseService cbService;
	private StorageService storeService;

	public CouchbaseRepository(final String repositoryId,
			final CouchbaseTypeManager typeManager) {
		// check repository id
		if (repositoryId == null || repositoryId.trim().length() == 0) {
			throw new IllegalArgumentException("Invalid repository id!");
		}

		this.repositoryId = repositoryId;

		// root = new File(rootPath);
		/*
		 * if (!root.isDirectory()) { throw new
		 * IllegalArgumentException("Root is not a directory!"); }
		 */

		// set type manager objects
		this.typeManager = typeManager;

		// set up read-write user map
		readWriteUserMap = new HashMap<String, Boolean>();

		// set up repository infos
		repositoryInfo10 = createRepositoryInfo(CmisVersion.CMIS_1_0);
		repositoryInfo11 = createRepositoryInfo(CmisVersion.CMIS_1_1);

		// couchbase
		cbService = CouchbaseService.getInstance();
		debug("Connection to cbservice = " + cbService);

	}

	public void setStorageService(StorageService storageService) {
		debug("storage service set");
		this.storeService = storageService;
	}

	private RepositoryInfo createRepositoryInfo(CmisVersion cmisVersion) {
		debug("createRepositoryInfo");
		assert cmisVersion != null;

		RepositoryInfoImpl repositoryInfo = new RepositoryInfoImpl();

		repositoryInfo.setId(repositoryId);
		repositoryInfo.setName(repositoryId);
		repositoryInfo.setDescription(repositoryId);

		repositoryInfo.setCmisVersionSupported(cmisVersion.value());

		repositoryInfo.setProductName("OpenCMIS FileShare");
		repositoryInfo.setProductVersion(ServerVersion.OPENCMIS_VERSION);
		repositoryInfo.setVendorName("OpenCMIS");

		repositoryInfo.setRootFolder(ROOT_ID);

		repositoryInfo.setThinClientUri("");
		repositoryInfo.setChangesIncomplete(true);

		RepositoryCapabilitiesImpl capabilities = new RepositoryCapabilitiesImpl();
		capabilities.setCapabilityAcl(CapabilityAcl.DISCOVER);
		capabilities.setAllVersionsSearchable(false);
		capabilities.setCapabilityJoin(CapabilityJoin.NONE);
		capabilities.setSupportsMultifiling(false);
		capabilities.setSupportsUnfiling(false);
		capabilities.setSupportsVersionSpecificFiling(false);
		capabilities.setIsPwcSearchable(false);
		capabilities.setIsPwcUpdatable(false);
		capabilities.setCapabilityQuery(CapabilityQuery.NONE);
		capabilities.setCapabilityChanges(CapabilityChanges.NONE);
		capabilities
				.setCapabilityContentStreamUpdates(CapabilityContentStreamUpdates.ANYTIME);
		capabilities.setSupportsGetDescendants(true);
		capabilities.setSupportsGetFolderTree(true);
		capabilities.setCapabilityRendition(CapabilityRenditions.NONE);

		if (cmisVersion != CmisVersion.CMIS_1_0) {
			capabilities.setCapabilityOrderBy(CapabilityOrderBy.NONE);

			NewTypeSettableAttributesImpl typeSetAttributes = new NewTypeSettableAttributesImpl();
			typeSetAttributes.setCanSetControllableAcl(false);
			typeSetAttributes.setCanSetControllablePolicy(false);
			typeSetAttributes.setCanSetCreatable(false);
			typeSetAttributes.setCanSetDescription(false);
			typeSetAttributes.setCanSetDisplayName(false);
			typeSetAttributes.setCanSetFileable(false);
			typeSetAttributes.setCanSetFulltextIndexed(false);
			typeSetAttributes.setCanSetId(false);
			typeSetAttributes.setCanSetIncludedInSupertypeQuery(false);
			typeSetAttributes.setCanSetLocalName(false);
			typeSetAttributes.setCanSetLocalNamespace(false);
			typeSetAttributes.setCanSetQueryable(false);
			typeSetAttributes.setCanSetQueryName(false);

			capabilities.setNewTypeSettableAttributes(typeSetAttributes);

			CreatablePropertyTypesImpl creatablePropertyTypes = new CreatablePropertyTypesImpl();
			capabilities.setCreatablePropertyTypes(creatablePropertyTypes);
		}

		repositoryInfo.setCapabilities(capabilities);

		AclCapabilitiesDataImpl aclCapability = new AclCapabilitiesDataImpl();
		aclCapability.setSupportedPermissions(SupportedPermissions.BASIC);
		aclCapability.setAclPropagation(AclPropagation.OBJECTONLY);

		// permissions
		List<PermissionDefinition> permissions = new ArrayList<PermissionDefinition>();
		permissions.add(createPermission(BasicPermissions.READ, "Read"));
		permissions.add(createPermission(BasicPermissions.WRITE, "Write"));
		permissions.add(createPermission(BasicPermissions.ALL, "All"));
		aclCapability.setPermissionDefinitionData(permissions);

		// mapping
		List<PermissionMapping> list = new ArrayList<PermissionMapping>();
		list.add(createMapping(PermissionMapping.CAN_CREATE_DOCUMENT_FOLDER,
				BasicPermissions.READ));
		list.add(createMapping(PermissionMapping.CAN_CREATE_FOLDER_FOLDER,
				BasicPermissions.READ));
		list.add(createMapping(PermissionMapping.CAN_DELETE_CONTENT_DOCUMENT,
				BasicPermissions.WRITE));
		list.add(createMapping(PermissionMapping.CAN_DELETE_OBJECT,
				BasicPermissions.ALL));
		list.add(createMapping(PermissionMapping.CAN_DELETE_TREE_FOLDER,
				BasicPermissions.ALL));
		list.add(createMapping(PermissionMapping.CAN_GET_ACL_OBJECT,
				BasicPermissions.READ));
		list.add(createMapping(
				PermissionMapping.CAN_GET_ALL_VERSIONS_VERSION_SERIES,
				BasicPermissions.READ));
		list.add(createMapping(PermissionMapping.CAN_GET_CHILDREN_FOLDER,
				BasicPermissions.READ));
		list.add(createMapping(PermissionMapping.CAN_GET_DESCENDENTS_FOLDER,
				BasicPermissions.READ));
		list.add(createMapping(PermissionMapping.CAN_GET_FOLDER_PARENT_OBJECT,
				BasicPermissions.READ));
		list.add(createMapping(PermissionMapping.CAN_GET_PARENTS_FOLDER,
				BasicPermissions.READ));
		list.add(createMapping(PermissionMapping.CAN_GET_PROPERTIES_OBJECT,
				BasicPermissions.READ));
		list.add(createMapping(PermissionMapping.CAN_MOVE_OBJECT,
				BasicPermissions.WRITE));
		list.add(createMapping(PermissionMapping.CAN_MOVE_SOURCE,
				BasicPermissions.READ));
		list.add(createMapping(PermissionMapping.CAN_MOVE_TARGET,
				BasicPermissions.WRITE));
		list.add(createMapping(PermissionMapping.CAN_SET_CONTENT_DOCUMENT,
				BasicPermissions.WRITE));
		list.add(createMapping(PermissionMapping.CAN_UPDATE_PROPERTIES_OBJECT,
				BasicPermissions.WRITE));
		list.add(createMapping(PermissionMapping.CAN_VIEW_CONTENT_OBJECT,
				BasicPermissions.READ));
		Map<String, PermissionMapping> map = new LinkedHashMap<String, PermissionMapping>();
		for (PermissionMapping pm : list) {
			map.put(pm.getKey(), pm);
		}
		aclCapability.setPermissionMappingData(map);

		repositoryInfo.setAclCapabilities(aclCapability);

		return repositoryInfo;
	}

	private PermissionDefinition createPermission(String permission,
			String description) {
		PermissionDefinitionDataImpl pd = new PermissionDefinitionDataImpl();
		pd.setId(permission);
		pd.setDescription(description);

		return pd;
	}

	private PermissionMapping createMapping(String key, String permission) {
		PermissionMappingDataImpl pm = new PermissionMappingDataImpl();
		pm.setKey(key);
		pm.setPermissions(Collections.singletonList(permission));

		return pm;
	}

	/**
	 * Returns the id of this repository.
	 */
	public String getRepositoryId() {
		return repositoryId;
	}

	/**
	 * Returns the root directory of this repository
	 */
	/*
	 * public File getRootDirectory() { return root; }
	 */

	/**
	 * Sets read-only flag for the given user.
	 */
	public void setUserReadOnly(String user) {
		if (user == null || user.length() == 0) {
			return;
		}

		readWriteUserMap.put(user, true);
	}

	/**
	 * Sets read-write flag for the given user.
	 */
	public void setUserReadWrite(String user) {
		if (user == null || user.length() == 0) {
			return;
		}

		readWriteUserMap.put(user, false);
	}

	// --- CMIS operations ---

	/**
	 * CMIS getRepositoryInfo.
	 */
	public RepositoryInfo getRepositoryInfo(CallContext context) {
		debug("getRepositoryInfo");

		checkUser(context, false);

		if (context.getCmisVersion() == CmisVersion.CMIS_1_0) {
			return repositoryInfo10;
		} else {
			return repositoryInfo11;
		}
	}

	/**
	 * CMIS getTypesChildren.
	 */
	public TypeDefinitionList getTypeChildren(CallContext context,
			String typeId, Boolean includePropertyDefinitions,
			BigInteger maxItems, BigInteger skipCount) {
		debug("getTypesChildren");
		checkUser(context, false);

		return typeManager.getTypeChildren(context, typeId,
				includePropertyDefinitions, maxItems, skipCount);
	}

	/**
	 * CMIS getTypesDescendants.
	 */
	public List<TypeDefinitionContainer> getTypeDescendants(
			CallContext context, String typeId, BigInteger depth,
			Boolean includePropertyDefinitions) {
		debug("getTypesDescendants");
		checkUser(context, false);

		return typeManager.getTypeDescendants(context, typeId, depth,
				includePropertyDefinitions);
	}

	/**
	 * CMIS getTypeDefinition.
	 */
	public TypeDefinition getTypeDefinition(CallContext context, String typeId) {
		debug("getTypeDefinition");
		checkUser(context, false);

		return typeManager.getTypeDefinition(context, typeId);
	}

	/**
	 * Create* dispatch for AtomPub.
	 */
	public ObjectData create(CallContext context, Properties properties,
			String folderId, ContentStream contentStream,
			VersioningState versioningState, ObjectInfoHandler objectInfos) {
		debug("create in folderId=" + folderId);
		boolean userReadOnly = checkUser(context, true);

		String typeId = CouchbaseUtils.getObjectTypeId(properties);
		TypeDefinition type = typeManager.getInternalTypeDefinition(typeId);
		if (type == null) {
			throw new CmisObjectNotFoundException("Type '" + typeId
					+ "' is unknown!");
		}

		CmisObject data = null;
		String dataId = null;
		if (type.getBaseTypeId() == BaseTypeId.CMIS_DOCUMENT) {
			dataId = createDocument(context, properties, folderId,
					contentStream, versioningState);

		} else if (type.getBaseTypeId() == BaseTypeId.CMIS_FOLDER) {
			dataId = createFolder(context, properties, folderId);

		} else {
			throw new CmisObjectNotFoundException(
					"Cannot create object of type '" + typeId + "'!");
		}

		if (dataId == null)
			throw new CmisObjectNotFoundException(
					"Cannot create object of type '" + typeId + "'!");

		data = cbService.getCmisObject(dataId);
		if (data == null)
			throw new CmisObjectNotFoundException(
					"Cannot create object of type '" + typeId + "'!");

		debug("create compileObjectData objectId:" + data.getId());

		return compileObjectData(context, data, null, false, false,
				userReadOnly, objectInfos);

	}

	/**
	 * CMIS createDocument.
	 */
	public String createDocument(CallContext context, Properties properties,
			String folderId, ContentStream contentStream,
			VersioningState versioningState) {
		debug("createDocument in folder " + folderId);
		checkUser(context, true);

		// check properties
		if (properties == null || properties.getProperties() == null) {
			throw new CmisInvalidArgumentException("Properties must be set!");
		}

		// check versioning state
		if (!(VersioningState.NONE == versioningState || versioningState == null)) {
			throw new CmisConstraintException("Versioning not supported!");
		}

		// check type
		String typeId = CouchbaseUtils.getObjectTypeId(properties);
		TypeDefinition type = typeManager.getInternalTypeDefinition(typeId);
		if (type == null) {
			throw new CmisObjectNotFoundException("Type '" + typeId
					+ "' is unknown!");
		}
		if (type.getBaseTypeId() != BaseTypeId.CMIS_DOCUMENT) {
			throw new CmisInvalidArgumentException(
					"Type must be a document type!");
		}

		// compile the properties
		PropertiesImpl props = compileWriteProperties(typeId,
				context.getUsername(), context.getUsername(), properties);

		// check the name
		String name = CouchbaseUtils.getStringProperty(properties,
				PropertyIds.NAME);
		if (!isValidName(name)) {
			throw new CmisNameConstraintViolationException("Name is not valid!");
		}

		// get parent File
		// File parent = getFile(folderId);
		CmisObject parentData = cbService.getCmisObject(folderId);
		if (parentData == null)
			throw new CmisStorageException("Parent folder doesn't exist!");

		if (!parentData.isDirectory()) {
			throw new CmisObjectNotFoundException("Parent is not a folder!");
		}

		// create the metadata in couchbase
		try {
			CmisObject data = cbService.createDocument(parentData, name,
					context.getUsername(), contentStream);

			// write content, if available
			if (contentStream != null && contentStream.getStream() != null) {
				storeService.writeContent(data.getId(), contentStream);
			}

			// set creation date
			addPropertyDateTime(props, typeId, null, PropertyIds.CREATION_DATE,
					data.getLastModificationDate());

			// write properties
			// writePropertiesFile(newFile, props);

			return data.getId();
		} catch (CouchbaseException e) {
			throw new CmisStorageException("Could not create metadata file: "
					+ e.getMessage(), e);
		} catch (StorageException e) {
			throw new CmisStorageException("Could not store file: "
					+ e.getMessage(), e);
		}
	}

	/**
	 * CMIS createDocumentFromSource.
	 */
	public String createDocumentFromSource(CallContext context,
			String sourceId, Properties properties, String folderId,
			VersioningState versioningState) {
		debug("createDocumentFromSource noyt yet implemented");
		/*
		 * checkUser(context, true);
		 * 
		 * // check versioning state if (!(VersioningState.NONE ==
		 * versioningState || versioningState == null)) { throw new
		 * CmisConstraintException("Versioning not supported!"); }
		 * 
		 * // get parent File File parent = getFile(folderId); if
		 * (!parent.isDirectory()) { throw new
		 * CmisObjectNotFoundException("Parent is not a folder!"); }
		 * 
		 * // get source File File source = getFile(sourceId); if
		 * (!source.isFile()) { throw new
		 * CmisObjectNotFoundException("Source is not a document!"); }
		 * 
		 * // file name String name = source.getName();
		 * 
		 * // get properties PropertiesImpl sourceProperties = new
		 * PropertiesImpl(); readCustomProperties(source, sourceProperties,
		 * null, new ObjectInfoImpl());
		 * 
		 * // get the type id String typeId =
		 * CouchbaseUtils.getIdProperty(sourceProperties,
		 * PropertyIds.OBJECT_TYPE_ID); if (typeId == null) { typeId =
		 * BaseTypeId.CMIS_DOCUMENT.value(); }
		 * 
		 * // copy properties PropertiesImpl newProperties = new
		 * PropertiesImpl(); for (PropertyData<?> prop :
		 * sourceProperties.getProperties().values()) { if
		 * (prop.getId().equals(PropertyIds.OBJECT_TYPE_ID) ||
		 * prop.getId().equals(PropertyIds.CREATED_BY) ||
		 * prop.getId().equals(PropertyIds.CREATION_DATE) ||
		 * prop.getId().equals(PropertyIds.LAST_MODIFIED_BY)) { continue; }
		 * 
		 * newProperties.addProperty(prop); }
		 * 
		 * // replace properties if (properties != null) { // find new name
		 * String newName = CouchbaseUtils.getStringProperty(properties,
		 * PropertyIds.NAME); if (newName != null) { if (!isValidName(newName))
		 * { throw new CmisNameConstraintViolationException(
		 * "Name is not valid!"); } name = newName; }
		 * 
		 * // get the property definitions TypeDefinition type =
		 * typeManager.getInternalTypeDefinition(typeId); if (type == null) {
		 * throw new CmisObjectNotFoundException("Type '" + typeId +
		 * "' is unknown!"); } if (type.getBaseTypeId() !=
		 * BaseTypeId.CMIS_DOCUMENT) { throw new CmisInvalidArgumentException(
		 * "Type must be a document type!"); }
		 * 
		 * // replace with new values for (PropertyData<?> prop :
		 * properties.getProperties().values()) { PropertyDefinition<?> propType
		 * = type.getPropertyDefinitions() .get(prop.getId());
		 * 
		 * // do we know that property? if (propType == null) { throw new
		 * CmisConstraintException("Property '" + prop.getId() +
		 * "' is unknown!"); }
		 * 
		 * // can it be set? if (propType.getUpdatability() !=
		 * Updatability.READWRITE) { throw new
		 * CmisConstraintException("Property '" + prop.getId() +
		 * "' cannot be updated!"); }
		 * 
		 * // empty properties are invalid if (isEmptyProperty(prop)) { throw
		 * new CmisConstraintException("Property '" + prop.getId() +
		 * "' must not be empty!"); }
		 * 
		 * newProperties.addProperty(prop); } }
		 * 
		 * addPropertyId(newProperties, typeId, null,
		 * PropertyIds.OBJECT_TYPE_ID, typeId); addPropertyString(newProperties,
		 * typeId, null, PropertyIds.CREATED_BY, context.getUsername());
		 * addPropertyDateTime(newProperties, typeId, null,
		 * PropertyIds.CREATION_DATE,
		 * CouchbaseUtils.millisToCalendar(System.currentTimeMillis()));
		 * addPropertyString(newProperties, typeId, null,
		 * PropertyIds.LAST_MODIFIED_BY, context.getUsername());
		 * 
		 * // check the file File newFile = new File(parent, name); if
		 * (newFile.exists()) { throw new CmisNameConstraintViolationException(
		 * "Document already exists."); }
		 * 
		 * // create the file try { newFile.createNewFile(); } catch
		 * (IOException e) { throw new
		 * CmisStorageException("Could not create file: " + e.getMessage(), e);
		 * }
		 * 
		 * // copy content try { writeContent(newFile, new
		 * FileInputStream(source)); } catch (IOException e) { throw new
		 * CmisStorageException("Could not roead or write content: " +
		 * e.getMessage(), e); }
		 * 
		 * // write properties writePropertiesFile(newFile, newProperties);
		 * 
		 * return getId(newFile);
		 */
		return null;
	}

	/**
	 * Writes the content to disc.
	 */
	private void writeContent(File newFile, InputStream stream) {
		OutputStream out = null;
		try {
			out = new FileOutputStream(newFile);
			IOUtils.copy(stream, out, BUFFER_SIZE);
		} catch (IOException e) {
			throw new CmisStorageException("Could not write content: "
					+ e.getMessage(), e);
		} finally {
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(stream);
		}
	}

	/**
	 * CMIS createFolder.
	 * 
	 * @return the objectId of the folder
	 */
	public String createFolder(CallContext context, Properties properties,
			String folderId) {
		debug("createFolder in folderId=" + folderId);
		checkUser(context, true);

		// check properties
		if (properties == null || properties.getProperties() == null) {
			throw new CmisInvalidArgumentException("Properties must be set!");
		}

		// check type
		String typeId = CouchbaseUtils.getObjectTypeId(properties);
		TypeDefinition type = typeManager.getInternalTypeDefinition(typeId);
		if (type == null) {
			throw new CmisObjectNotFoundException("Type '" + typeId
					+ "' is unknown!");
		}
		if (type.getBaseTypeId() != BaseTypeId.CMIS_FOLDER) {
			throw new CmisInvalidArgumentException(
					"Type must be a folder type!");
		}

		// compile the properties
		PropertiesImpl props = compileWriteProperties(typeId,
				context.getUsername(), context.getUsername(), properties);

		// check the name
		String name = CouchbaseUtils.getStringProperty(properties,
				PropertyIds.NAME);
		if (!isValidName(name)) {
			throw new CmisNameConstraintViolationException("Name is not valid.");
		}

		// get parent from couchbase
		try {
			debug("get parent with id:" + folderId);
			CmisObject parentData = cbService.getCmisObject(folderId);

			if (parentData == null)
				throw new CmisObjectNotFoundException("Parent is unknown !");

			if (!parentData.isDirectory()) {
				throw new CmisObjectNotFoundException("Parent is not a folder!");
			}

			// create the folder
			CmisObject folderData = cbService.createFolder(parentData, name,
					context.getUsername());

			debug("folderData=" + folderData);

			// set creation date
			addPropertyDateTime(props, typeId, null, PropertyIds.CREATION_DATE,
					folderData.getLastModificationDate());

			// write properties
			// writePropertiesFile(folderData, props);
			return folderData.getId();

		} catch (CouchbaseException e) {
			debug("Impossible to create folder " + folderId);
			return null;
		}

	}

	/**
	 * CMIS moveObject.
	 */
	public ObjectData moveObject(CallContext context, Holder<String> objectId,
			String targetFolderId, ObjectInfoHandler objectInfos) {
		debug("moveObject not yet implemented");
		return null;
	}

	/**
	 * CMIS setContentStream, deleteContentStream, and appendContentStream.
	 */
	public void changeContentStream(CallContext context,
			Holder<String> objectId, Boolean overwriteFlag,
			ContentStream contentStream, boolean append) {
		debug("setContentStream or deleteContentStream or appendContentStream not yet implemented");
		/*
		 * checkUser(context, true);
		 * 
		 * if (objectId == null) { throw new
		 * CmisInvalidArgumentException("Id is not valid!"); }
		 * 
		 * // get the file File file = getFile(objectId.getValue()); if
		 * (!file.isFile()) { throw new
		 * CmisStreamNotSupportedException("Not a file!"); }
		 * 
		 * // check overwrite boolean owf =
		 * CouchbaseUtils.getBooleanParameter(overwriteFlag, true); if (!owf &&
		 * file.length() > 0) { throw new CmisContentAlreadyExistsException(
		 * "Content already exists!"); }
		 * 
		 * OutputStream out = null; InputStream in = null; try { out = new
		 * FileOutputStream(file, append);
		 * 
		 * if (contentStream == null || contentStream.getStream() == null) { //
		 * delete content out.write(new byte[0]); } else { // set content in =
		 * contentStream.getStream(); IOUtils.copy(in, out, BUFFER_SIZE); } }
		 * catch (Exception e) { throw new
		 * CmisStorageException("Could not write content: " + e.getMessage(),
		 * e); } finally { IOUtils.closeQuietly(out); IOUtils.closeQuietly(in);
		 * }
		 */

	}

	/**
	 * CMIS deleteObject.
	 */
	public void deleteObject(CallContext context, String objectId) {
		debug("deleteObject objectId=" + objectId);
		checkUser(context, true);

		// get the file or folder
		debug("get the Cmis object objectId=" + objectId);
		CmisObject data = cbService.getCmisObject(objectId);
		if (data == null)
			throw new CmisObjectNotFoundException("Object not found!");

		debug("check if object exists objectId=" + objectId);

		// boolean contentExists = storeService.exists(objectId);

		/*
		 * if (!contentExists) throw new
		 * CmisObjectNotFoundException("Content not found!");
		 */

		// check if it is a folder and if it is empty
		if (data.isDirectory() && (!data.getChildren().isEmpty())) {
			throw new CmisConstraintException("Folder is not empty!");
		}

		// delete properties and actual file
		try {
			debug("delete properties on objectId=" + objectId);

			boolean propertiesDeleted = cbService.deleteProperties(data,
					context.getUsername());
			if (!propertiesDeleted) {
				throw new CmisStorageException("Deletion properties failed!");
			}
		} catch (CouchbaseException e) {
			throw new CmisStorageException("Deletion properties failed!");
		}
		if (!data.isDirectory()) {
			debug("delete content on objectId=" + objectId);

			boolean contentDeleted = storeService.deleteContent(objectId);
			if (!contentDeleted) {
				throw new CmisStorageException("Deletion content failed!");
			}
		}
	}

	/**
	 * CMIS deleteTree.
	 */
	public FailedToDeleteData deleteTree(CallContext context, String folderId,
			Boolean continueOnFailure) {
		debug("deleteTree not yet implemented");
		/*
		 * checkUser(context, true);
		 * 
		 * boolean cof = CouchbaseUtils.getBooleanParameter(continueOnFailure,
		 * false);
		 * 
		 * // get the file or folder File file = getFile(folderId);
		 * 
		 * FailedToDeleteDataImpl result = new FailedToDeleteDataImpl();
		 * result.setIds(new ArrayList<String>());
		 * 
		 * // if it is a folder, remove it recursively if (file.isDirectory()) {
		 * deleteFolder(file, cof, result); } else { throw new
		 * CmisConstraintException("Object is not a folder!"); }
		 * 
		 * return result;
		 */
		return null;
	}

	/**
	 * Removes a folder and its content.
	 */
	private boolean deleteFolder(File folder, boolean continueOnFailure,
			FailedToDeleteDataImpl ftd) {
		debug("deleteFolder not yet implemented");
		/*
		 * boolean success = true;
		 * 
		 * for (File file : folder.listFiles()) { if (file.isDirectory()) { if
		 * (!deleteFolder(file, continueOnFailure, ftd)) { if
		 * (!continueOnFailure) { return false; } success = false; } } else { if
		 * (!file.delete()) { ftd.getIds().add(getId(file)); if
		 * (!continueOnFailure) { return false; } success = false; } } }
		 * 
		 * if (!folder.delete()) { ftd.getIds().add(getId(folder)); success =
		 * false; }
		 * 
		 * return success;
		 */
		return true;
	}

	/**
	 * CMIS updateProperties.
	 */
	public ObjectData updateProperties(CallContext context,
			Holder<String> objectId, Properties properties,
			ObjectInfoHandler objectInfos) {
		debug("updateProperties not yet implemented");
		return null;
	}

	/**
	 * Checks and updates a property set that can be written to disc.
	 */
	private Properties updateProperties(String typeId, String creator,
			GregorianCalendar creationDate, String modifier,
			Properties oldProperties, Properties properties) {
		PropertiesImpl result = new PropertiesImpl();

		if (properties == null) {
			throw new CmisConstraintException("No properties!");
		}

		// get the property definitions
		TypeDefinition type = typeManager.getInternalTypeDefinition(typeId);
		if (type == null) {
			throw new CmisObjectNotFoundException("Type '" + typeId
					+ "' is unknown!");
		}

		// copy old properties
		for (PropertyData<?> prop : oldProperties.getProperties().values()) {
			PropertyDefinition<?> propType = type.getPropertyDefinitions().get(
					prop.getId());

			// do we know that property?
			if (propType == null) {
				throw new CmisConstraintException("Property '" + prop.getId()
						+ "' is unknown!");
			}

			// only add read/write properties
			if (propType.getUpdatability() != Updatability.READWRITE) {
				continue;
			}

			result.addProperty(prop);
		}

		// update properties
		for (PropertyData<?> prop : properties.getProperties().values()) {
			PropertyDefinition<?> propType = type.getPropertyDefinitions().get(
					prop.getId());

			// do we know that property?
			if (propType == null) {
				throw new CmisConstraintException("Property '" + prop.getId()
						+ "' is unknown!");
			}

			// can it be set?
			if (propType.getUpdatability() == Updatability.READONLY) {
				throw new CmisConstraintException("Property '" + prop.getId()
						+ "' is readonly!");
			}

			if (propType.getUpdatability() == Updatability.ONCREATE) {
				throw new CmisConstraintException("Property '" + prop.getId()
						+ "' can only be set on create!");
			}

			// default or value
			if (isEmptyProperty(prop)) {
				addPropertyDefault(result, propType);
			} else {
				result.addProperty(prop);
			}
		}

		addPropertyId(result, typeId, null, PropertyIds.OBJECT_TYPE_ID, typeId);
		addPropertyString(result, typeId, null, PropertyIds.CREATED_BY, creator);
		addPropertyDateTime(result, typeId, null, PropertyIds.CREATION_DATE,
				creationDate);
		addPropertyString(result, typeId, null, PropertyIds.LAST_MODIFIED_BY,
				modifier);

		return result;
	}

	/**
	 * CMIS bulkUpdateProperties.
	 */
	public List<BulkUpdateObjectIdAndChangeToken> bulkUpdateProperties(
			CallContext context,
			List<BulkUpdateObjectIdAndChangeToken> objectIdAndChangeToken,
			Properties properties, ObjectInfoHandler objectInfos) {
		debug("bulkUpdateProperties");
		checkUser(context, true);

		if (objectIdAndChangeToken == null) {
			throw new CmisInvalidArgumentException("No object ids provided!");
		}

		List<BulkUpdateObjectIdAndChangeToken> result = new ArrayList<BulkUpdateObjectIdAndChangeToken>();

		for (BulkUpdateObjectIdAndChangeToken oid : objectIdAndChangeToken) {
			if (oid == null) {
				// ignore invalid ids
				continue;
			}
			try {
				Holder<String> oidHolder = new Holder<String>(oid.getId());
				updateProperties(context, oidHolder, properties, objectInfos);

				result.add(new BulkUpdateObjectIdAndChangeTokenImpl(
						oid.getId(), oidHolder.getValue(), null));
			} catch (CmisBaseException e) {
				// ignore exceptions - see specification
			}
		}

		return result;
	}

	/**
	 * CMIS getObject.
	 */
	public ObjectData getObject(CallContext context, String objectId,
			String versionServicesId, String filter,
			Boolean includeAllowableActions, Boolean includeAcl,
			ObjectInfoHandler objectInfos) {
		debug("getObject");
		debug("getObject objectId:" + objectId + " - versionServicesId="
				+ versionServicesId);

		boolean userReadOnly = checkUser(context, false);

		// check id
		if (objectId == null && versionServicesId == null) {
			throw new CmisInvalidArgumentException("Object Id must be set.");
		}

		if (objectId == null) {
			// this works only because there are no versions in a file system
			// and the object id and version series id are the same
			objectId = versionServicesId;
		}

		// get the file or folder
		// File file = getFile(objectId);
		// String encodedObjectId = encode(objectId);
		// debug("encodedObjectId=" + encodedObjectId);
		CmisObject data = getCmisObject(objectId);
		debug("data : " + data);

		// set defaults if values not set
		boolean iaa = CouchbaseUtils.getBooleanParameter(
				includeAllowableActions, false);
		boolean iacl = CouchbaseUtils.getBooleanParameter(includeAcl, false);

		// split filter
		Set<String> filterCollection = CouchbaseUtils.splitFilter(filter);

		// gather properties
		debug("getObject compileObjectData file:" + objectId);
		return compileObjectData(context, data, filterCollection, iaa, iacl,
				userReadOnly, objectInfos);
	}

	/*
	 * private String encode(String objectId) { debug("encode objectId=" +
	 * objectId); // only root dir is supposed to be called like that if
	 * (ROOT_ID.equals(objectId)) { return ROOT_ID; }
	 * 
	 * return objectId; }
	 */

	public CmisObject getCmisObject(String objectId) {
		try {
			return this.cbService.getCmisObject(objectId);
		} catch (Exception e) {
			throw new CmisObjectNotFoundException(e.getMessage(), e);
		}
	}

	/**
	 * CMIS getAllowableActions.
	 */
	public AllowableActions getAllowableActions(CallContext context,
			String objectId) {
		debug("getAllowableActions not yet implemented");
		/*
		 * boolean userReadOnly = checkUser(context, false);
		 * 
		 * File file = getFile(objectId); if (!file.exists()) { throw new
		 * CmisObjectNotFoundException("Object not found!"); }
		 * 
		 * return compileAllowableActions(file, userReadOnly);
		 */
		return null;
	}

	/**
	 * CMIS getACL.
	 */
	public Acl getAcl(CallContext context, String objectId) {
		debug("getAcl not yet implemented");
		/*
		 * checkUser(context, false);
		 * 
		 * // get the file or folder File file = getFile(objectId); if
		 * (!file.exists()) { throw new
		 * CmisObjectNotFoundException("Object not found!"); }
		 * 
		 * return compileAcl(file);
		 */
		return null;
	}

	/**
	 * CMIS getContentStream.
	 */
	public ContentStream getContentStream(CallContext context, String objectId,
			BigInteger offset, BigInteger length) {
		debug("getContentStream objectId=" + objectId);
		try {
			System.out.println("getContentStream objectId=" + objectId);
			checkUser(context, false);

			// get the filename
			CmisObject data = cbService.getCmisObject(objectId);

			if (data == null)
				throw new CmisObjectNotFoundException(
						"this file does not exist : " + objectId);

			System.out.println("filename = " + data.getFileName());
			// get the file

			return storeService.getContent(objectId, offset, length,
					data.getFileName());
		} catch (StorageException e) {
			throw new CmisObjectNotFoundException(e.getMessage());
		}
	}

	/**
	 * CMIS getChildren.
	 */
	public ObjectInFolderList getChildren(CallContext context, String folderId,
			String filter, Boolean includeAllowableActions,
			Boolean includePathSegment, BigInteger maxItems,
			BigInteger skipCount, ObjectInfoHandler objectInfos) {
		debug("getChildren folderId=" + folderId);

		boolean userReadOnly = checkUser(context, false);

		// split filter
		Set<String> filterCollection = CouchbaseUtils.splitFilter(filter);

		// set defaults if values not set
		boolean iaa = CouchbaseUtils.getBooleanParameter(
				includeAllowableActions, false);
		boolean ips = CouchbaseUtils.getBooleanParameter(includePathSegment,
				false);

		// skip and max
		int skip = (skipCount == null ? 0 : skipCount.intValue());
		if (skip < 0) {
			skip = 0;
		}

		int max = (maxItems == null ? Integer.MAX_VALUE : maxItems.intValue());
		if (max < 0) {
			max = Integer.MAX_VALUE;
		}

		// get the folder
		// *************** TO BE CONTINUED
		// File folder = getFile(folderId);
		CmisObject data = getCmisObject(folderId);

		if (data == null) {
			throw new CmisObjectNotFoundException("Does not exist !");
		}

		if (!data.isDirectory()) {
			throw new CmisObjectNotFoundException("Not a folder!");
		}

		// set object info of the the folder
		if (context.isObjectInfoRequired()) {
			debug("getChildren compileObjectData ...");
			compileObjectData(context, data, null, false, false, userReadOnly,
					objectInfos);
		}

		// prepare result
		ObjectInFolderListImpl result = new ObjectInFolderListImpl();
		result.setObjects(new ArrayList<ObjectInFolderData>());
		result.setHasMoreItems(false);
		int count = 0;

		// iterate through children
		CmisObject child;
		for (String childId : data.getChildren()) {
			count++;

			if (skip > 0) {
				skip--;
				continue;
			}

			// TODO ca veut dire quoi ?
			if (result.getObjects().size() >= max) {
				result.setHasMoreItems(true);
				continue;
			}

			// build and add child object
			ObjectInFolderDataImpl objectInFolder = new ObjectInFolderDataImpl();
			child = getCmisObject(childId);
			if (child == null) {
				debug("This object is not stored in Couchbase : " + childId);
				continue; // move to the next object
			}
			objectInFolder.setObject(compileObjectData(context, child,
					filterCollection, iaa, false, userReadOnly, objectInfos));
			if (ips) {
				objectInFolder.setPathSegment(child.getName());
			}

			result.getObjects().add(objectInFolder);
		}

		result.setNumItems(BigInteger.valueOf(count));

		/*
		 * for (File child : folder.listFiles()) { // skip hidden and shadow
		 * files if (child.isHidden() || child.getName().equals(SHADOW_FOLDER)
		 * || child.getPath().endsWith(SHADOW_EXT)) { continue; }
		 * 
		 * count++;
		 * 
		 * if (skip > 0) { skip--; continue; }
		 * 
		 * if (result.getObjects().size() >= max) {
		 * result.setHasMoreItems(true); continue; }
		 * 
		 * // build and add child object ObjectInFolderDataImpl objectInFolder =
		 * new ObjectInFolderDataImpl();
		 * debug("getChildren compileObjectData for subfolder :"
		 * +child.getAbsolutePath());
		 * objectInFolder.setObject(compileObjectData(context, child,
		 * filterCollection, iaa, false, userReadOnly, objectInfos)); if (ips) {
		 * objectInFolder.setPathSegment(child.getName()); }
		 * 
		 * result.getObjects().add(objectInFolder); }
		 * 
		 * result.setNumItems(BigInteger.valueOf(count));
		 */

		return result;
	}

	/**
	 * CMIS getDescendants.
	 */
	public List<ObjectInFolderContainer> getDescendants(CallContext context,
			String folderId, BigInteger depth, String filter,
			Boolean includeAllowableActions, Boolean includePathSegment,
			ObjectInfoHandler objectInfos, boolean foldersOnly) {
		debug("getDescendants or getFolderTree not yet implemented");
		return null;
	}

	/**
	 * Gather the children of a folder.
	 */
	private void gatherDescendants(CallContext context, File folder,
			List<ObjectInFolderContainer> list, boolean foldersOnly, int depth,
			Set<String> filter, boolean includeAllowableActions,
			boolean includePathSegments, boolean userReadOnly,
			ObjectInfoHandler objectInfos) {
		debug("gatherDescendants not yet implemented");
	}

	/**
	 * CMIS getFolderParent.
	 */
	public ObjectData getFolderParent(CallContext context, String folderId,
			String filter, ObjectInfoHandler objectInfos) {
		debug("getFolderParent folderId=" + folderId);

		List<ObjectParentData> parents = getObjectParents(context, folderId,
				filter, false, false, objectInfos);

		if (parents.isEmpty()) {
			throw new CmisInvalidArgumentException(
					"The root folder has no parent!");
		}

		return parents.get(0).getObject();
	}

	/**
	 * CMIS getObjectParents.
	 */
	public List<ObjectParentData> getObjectParents(CallContext context,
			String objectId, String filter, Boolean includeAllowableActions,
			Boolean includeRelativePathSegment, ObjectInfoHandler objectInfos) {
		debug("getObjectParents not yet implemented");
		debug("getObjectParents objectId:" + objectId);
		boolean userReadOnly = checkUser(context, false);

		// split filter
		Set<String> filterCollection = CouchbaseUtils.splitFilter(filter);

		// set defaults if values not set
		boolean iaa = CouchbaseUtils.getBooleanParameter(
				includeAllowableActions, false);
		boolean irps = CouchbaseUtils.getBooleanParameter(
				includeRelativePathSegment, false);

		// get the file or folder
		// File file = getFile(objectId);
		CmisObject data = cbService.getCmisObject(objectId);

		if (data == null) {
			return null;
		}

		// don't climb above the root folder
		if (data.isRoot()) {
			return Collections.emptyList();
		}

		// set object info of the the object
		if (context.isObjectInfoRequired()) {
			debug("getObjectParents compileObjectData file:" + data.getName());
			compileObjectData(context, data, null, false, false, userReadOnly,
					objectInfos);
		}

		// get parent folder
		CmisObject parentData = cbService.getCmisObject(data.getParentId());
		if (parentData == null)
			return null;

		debug("getObjectParents compileObjectData parent:"
				+ parentData.getName());
		ObjectData object = compileObjectData(context, parentData,
				filterCollection, iaa, false, userReadOnly, objectInfos);

		ObjectParentDataImpl result = new ObjectParentDataImpl();
		result.setObject(object);
		if (irps) {
			result.setRelativePathSegment(data.getName());
		}

		return Collections.<ObjectParentData> singletonList(result);
	}

	/**
	 * CMIS getObjectByPath.
	 */
	public ObjectData getObjectByPath(CallContext context, String folderPath,
			String filter, boolean includeAllowableActions, boolean includeACL,
			ObjectInfoHandler objectInfos) {
		debug("getObjectByPath not yet implemented");
		return null;
	}

	// --- helpers ---

	/**
	 * Compiles an object type object from a file or folder.
	 */
	private ObjectData compileObjectData(CallContext context, CmisObject data,
			Set<String> filter, boolean includeAllowableActions,
			boolean includeAcl, boolean userReadOnly,
			ObjectInfoHandler objectInfos) {

		ObjectDataImpl result = new ObjectDataImpl();
		ObjectInfoImpl objectInfo = new ObjectInfoImpl();

		result.setProperties(compileProperties(context, data, filter,
				objectInfo));

		if (includeAllowableActions) {
			debug("Allowable actions not yet implemented");
			/*
			 * result.setAllowableActions(compileAllowableActions(file,
			 * userReadOnly));
			 */
		}

		if (includeAcl) {
			debug("ACL not yet implemented");
			/*
			 * result.setAcl(compileAcl(file)); result.setIsExactAcl(true);
			 */
		}

		if (context.isObjectInfoRequired()) {
			objectInfo.setObject(result);
			objectInfos.addObjectInfo(objectInfo);
			debug("addObjectInfo objectInfo:" + objectInfo);
		}

		debug("compileObjectData done.");
		return result;
	}

	/*
	 * 
	 * private ObjectData compileObjectData(CallContext context, File file,
	 * Set<String> filter, boolean includeAllowableActions, boolean includeAcl,
	 * boolean userReadOnly, ObjectInfoHandler objectInfos) {
	 * debug("compileObjectData file:" + file.getAbsolutePath()); ObjectDataImpl
	 * result = new ObjectDataImpl(); ObjectInfoImpl objectInfo = new
	 * ObjectInfoImpl();
	 * 
	 * result.setProperties(compileProperties(context, file, filter,
	 * objectInfo));
	 * 
	 * if (includeAllowableActions) {
	 * result.setAllowableActions(compileAllowableActions(file, userReadOnly));
	 * }
	 * 
	 * if (includeAcl) { result.setAcl(compileAcl(file));
	 * result.setIsExactAcl(true); }
	 * 
	 * if (context.isObjectInfoRequired()) { objectInfo.setObject(result);
	 * objectInfos.addObjectInfo(objectInfo); debug("addObjectInfo objectInfo:"
	 * + objectInfo); }
	 * 
	 * debug("compileObjectData done."); return result; }
	 */

	/**
	 * Gathers all base properties of a file or folder.
	 */
	private Properties compileProperties(CallContext context, CmisObject data,
			Set<String> orgfilter, ObjectInfoImpl objectInfo) {
		debug("compileProperties ...");
		if (data == null) {
			debug("compileProperties data must not be null!");
			throw new IllegalArgumentException("data must not be null!");
		}

		// we can't gather properties if the file or folder doesn't exist
		if (!data.exists()) {
			debug("compileProperties Object not found!");
			throw new CmisObjectNotFoundException("Object not found!");
		}

		// copy filter
		Set<String> filter = (orgfilter == null ? null : new HashSet<String>(
				orgfilter));

		// find base type
		String typeId = null;

		if (data.isDirectory()) {
			typeId = BaseTypeId.CMIS_FOLDER.value();
			debug("compileProperties typeId=" + typeId);
			objectInfo.setBaseType(BaseTypeId.CMIS_FOLDER);
			objectInfo.setTypeId(typeId);
			objectInfo.setContentType(null);
			objectInfo.setFileName(null);
			objectInfo.setHasAcl(true);
			objectInfo.setHasContent(false);
			objectInfo.setVersionSeriesId(null);
			objectInfo.setIsCurrentVersion(true);
			objectInfo.setRelationshipSourceIds(null);
			objectInfo.setRelationshipTargetIds(null);
			objectInfo.setRenditionInfos(null);
			objectInfo.setSupportsDescendants(true);
			objectInfo.setSupportsFolderTree(true);
			objectInfo.setSupportsPolicies(false);
			objectInfo.setSupportsRelationships(false);
			objectInfo.setWorkingCopyId(null);
			objectInfo.setWorkingCopyOriginalId(null);
		} else {
			typeId = BaseTypeId.CMIS_DOCUMENT.value();
			debug("compileProperties typeId=" + typeId);
			objectInfo.setBaseType(BaseTypeId.CMIS_DOCUMENT);
			objectInfo.setTypeId(typeId);
			objectInfo.setHasAcl(true);
			objectInfo.setHasContent(true);
			objectInfo.setHasParent(true);
			objectInfo.setVersionSeriesId(null);
			objectInfo.setIsCurrentVersion(true);
			objectInfo.setRelationshipSourceIds(null);
			objectInfo.setRelationshipTargetIds(null);
			objectInfo.setRenditionInfos(null);
			objectInfo.setSupportsDescendants(false);
			objectInfo.setSupportsFolderTree(false);
			objectInfo.setSupportsPolicies(false);
			objectInfo.setSupportsRelationships(false);
			objectInfo.setWorkingCopyId(null);
			objectInfo.setWorkingCopyOriginalId(null);
		}

		// let's do it
		try {
			PropertiesImpl result = new PropertiesImpl();

			// id
			String id = data.getId();
			addPropertyId(result, typeId, filter, PropertyIds.OBJECT_ID, id);
			objectInfo.setId(id);
			debug("compileProperties objectinfo.id=" + objectInfo.getId());

			// name
			String name = data.getName();
			addPropertyString(result, typeId, filter, PropertyIds.NAME, name);
			objectInfo.setName(name);
			debug("compileProperties objectinfo.name=" + objectInfo.getName());

			// created and modified by
			addPropertyString(result, typeId, filter, PropertyIds.CREATED_BY,
					data.getCreatedBy());
			objectInfo.setCreatedBy(data.getCreatedBy());

			addPropertyString(result, typeId, filter,
					PropertyIds.LAST_MODIFIED_BY, data.getLastModifiedBy());

			// creation and modification date
			GregorianCalendar creationDate = data.getCreationDate();

			addPropertyDateTime(result, typeId, filter,
					PropertyIds.CREATION_DATE, creationDate);
			objectInfo.setCreationDate(creationDate);

			GregorianCalendar lastModified = data.getLastModificationDate();
			addPropertyDateTime(result, typeId, filter,
					PropertyIds.LAST_MODIFICATION_DATE, lastModified);
			objectInfo.setLastModificationDate(lastModified);

			// change token - always null
			addPropertyString(result, typeId, filter, PropertyIds.CHANGE_TOKEN,
					null);

			// CMIS 1.1 properties
			if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
				addPropertyString(result, typeId, filter,
						PropertyIds.DESCRIPTION, null);
				addPropertyIdList(result, typeId, filter,
						PropertyIds.SECONDARY_OBJECT_TYPE_IDS, null);
			}

			// directory or file
			if (data.isDirectory()) {
				debug("compileProperties isDirectory");

				// base type and type name
				addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID,
						BaseTypeId.CMIS_FOLDER.value());
				addPropertyId(result, typeId, filter,
						PropertyIds.OBJECT_TYPE_ID,
						BaseTypeId.CMIS_FOLDER.value());
				// String path = getRepositoryPath(file);
				String path = data.getFullPath();
				addPropertyString(result, typeId, filter, PropertyIds.PATH,
						path);
				debug("compileProperties repopath=" + path);

				// folder properties
				if (!data.isRoot()) {
					/*
					 * addPropertyId(result, typeId, filter,
					 * PropertyIds.PARENT_ID, (root.equals(file.getParentFile())
					 * ? ROOT_ID : fileToId(file.getParentFile())));
					 * objectInfo.setHasParent(true);
					 */
					String parentId = data.getParentId();
					addPropertyId(result, typeId, filter,
							PropertyIds.PARENT_ID, parentId);
					objectInfo.setHasParent(true);
					System.out
							.println("compileProperties parentId=" + parentId);

				} else {
					addPropertyId(result, typeId, filter,
							PropertyIds.PARENT_ID, null);
					objectInfo.setHasParent(false);
				}

				debug("compileProperties hasParent=" + objectInfo.hasParent());

				addPropertyIdList(result, typeId, filter,
						PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, null);
			} else {
				debug("compileProperties isDocument");

				// base type and type name
				addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID,
						BaseTypeId.CMIS_DOCUMENT.value());
				addPropertyId(result, typeId, filter,
						PropertyIds.OBJECT_TYPE_ID,
						BaseTypeId.CMIS_DOCUMENT.value());

				// file properties
				addPropertyBoolean(result, typeId, filter,
						PropertyIds.IS_IMMUTABLE, false);
				addPropertyBoolean(result, typeId, filter,
						PropertyIds.IS_LATEST_VERSION, true);
				addPropertyBoolean(result, typeId, filter,
						PropertyIds.IS_MAJOR_VERSION, true);
				addPropertyBoolean(result, typeId, filter,
						PropertyIds.IS_LATEST_MAJOR_VERSION, true);
				addPropertyString(result, typeId, filter,
						PropertyIds.VERSION_LABEL, data.getFileName());
				addPropertyId(result, typeId, filter,
						PropertyIds.VERSION_SERIES_ID, data.getId());
				addPropertyBoolean(result, typeId, filter,
						PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false);
				addPropertyString(result, typeId, filter,
						PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, null);
				addPropertyString(result, typeId, filter,
						PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, null);
				addPropertyString(result, typeId, filter,
						PropertyIds.CHECKIN_COMMENT, "");
				if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
					addPropertyBoolean(result, typeId, filter,
							PropertyIds.IS_PRIVATE_WORKING_COPY, false);
				}

				// TODO
				addPropertyBigInteger(result, typeId, filter,
						PropertyIds.CONTENT_STREAM_LENGTH, null);
				addPropertyString(result, typeId, filter,
						PropertyIds.CONTENT_STREAM_MIME_TYPE, null);
				addPropertyString(result, typeId, filter,
						PropertyIds.CONTENT_STREAM_FILE_NAME, null);

				// file content
				objectInfo.setHasContent(true);
				objectInfo.setFileName(data.getFileName());
				objectInfo.setContentType(data.getContentType());

				addPropertyId(result, typeId, filter,
						PropertyIds.CONTENT_STREAM_ID, null);
			}

			// read custom properties
			debug("readCustomProperties not yet implemented");
			// readCustomProperties(data, result, filter, objectInfo);
			// debug("readCustomProperties done.");

			if (filter != null) {
				if (!filter.isEmpty()) {
					debug("Unknown filter properties: " + filter.toString());
				}
			}

			debug("objectInfo.getId : " + objectInfo.getId());
			debug("objectInfo.getAtomId : " + objectInfo.getAtomId());
			debug("objectInfo.getName : " + objectInfo.getName());
			debug("objectInfo.getCreatedBy : " + objectInfo.getCreatedBy());
			debug("objectInfo.getCreationDate : "
					+ objectInfo.getCreationDate());
			debug("objectInfo.getLastModificationDate : "
					+ objectInfo.getLastModificationDate());
			debug("objectInfo.getBaseType : " + objectInfo.getBaseType());
			debug("objectInfo.isCurrentVersion : "
					+ objectInfo.isCurrentVersion());
			debug("objectInfo.getVersionSeriesId : "
					+ objectInfo.getVersionSeriesId());
			debug("objectInfo.getWorkingCopyId : "
					+ objectInfo.getWorkingCopyId());
			debug("objectInfo.getWorkingCopyOriginalId : "
					+ objectInfo.getWorkingCopyOriginalId());
			debug("objectInfo.hasContent : " + objectInfo.hasContent());
			debug("objectInfo.getContentType : " + objectInfo.getContentType());
			debug("objectInfo.getFileName : " + objectInfo.getFileName());
			debug("objectInfo.getRenditionInfos : "
					+ objectInfo.getRenditionInfos());
			debug("objectInfo.supportsRelationships : "
					+ objectInfo.supportsRelationships());
			debug("objectInfo.supportsPolicies : "
					+ objectInfo.supportsPolicies());
			debug("objectInfo.hasAcl : " + objectInfo.hasAcl());
			debug("objectInfo.hasParent : " + objectInfo.hasParent());
			debug("objectInfo.supportsDescendants : "
					+ objectInfo.supportsDescendants());
			debug("objectInfo.supportsFolderTree : "
					+ objectInfo.supportsFolderTree());
			debug("objectInfo.getRelationshipSourceIds : "
					+ objectInfo.getRelationshipSourceIds());
			debug("objectInfo.getRelationshipTargetIds : "
					+ objectInfo.getRelationshipTargetIds());
			debug("objectInfo.getAdditionalLinks : "
					+ objectInfo.getAdditionalLinks());
			debug("objectInfo.getObject : " + objectInfo.getObject());

			debug("compileProperties done.");

			return result;
		} catch (CmisBaseException cbe) {
			throw cbe;
		} catch (Exception e) {
			throw new CmisRuntimeException(e.getMessage(), e);
		}
	}

	/*
	 * private Properties compileProperties(CallContext context, File file,
	 * Set<String> orgfilter, ObjectInfoImpl objectInfo) {
	 * debug("compileProperties ..."); if (file == null) {
	 * debug("compileProperties File must not be null!"); throw new
	 * IllegalArgumentException("File must not be null!"); }
	 * 
	 * // we can't gather properties if the file or folder doesn't exist if
	 * (!file.exists()) { debug("compileProperties Object not found!"); throw
	 * new CmisObjectNotFoundException("Object not found!"); }
	 * 
	 * // copy filter Set<String> filter = (orgfilter == null ? null : new
	 * HashSet<String>( orgfilter));
	 * 
	 * // find base type String typeId = null;
	 * 
	 * if (file.isDirectory()) { typeId = BaseTypeId.CMIS_FOLDER.value();
	 * debug("compileProperties typeId=" + typeId);
	 * objectInfo.setBaseType(BaseTypeId.CMIS_FOLDER);
	 * objectInfo.setTypeId(typeId); objectInfo.setContentType(null);
	 * objectInfo.setFileName(null); objectInfo.setHasAcl(true);
	 * objectInfo.setHasContent(false); objectInfo.setVersionSeriesId(null);
	 * objectInfo.setIsCurrentVersion(true);
	 * objectInfo.setRelationshipSourceIds(null);
	 * objectInfo.setRelationshipTargetIds(null);
	 * objectInfo.setRenditionInfos(null);
	 * objectInfo.setSupportsDescendants(true);
	 * objectInfo.setSupportsFolderTree(true);
	 * objectInfo.setSupportsPolicies(false);
	 * objectInfo.setSupportsRelationships(false);
	 * objectInfo.setWorkingCopyId(null);
	 * objectInfo.setWorkingCopyOriginalId(null); } else { typeId =
	 * BaseTypeId.CMIS_DOCUMENT.value(); debug("compileProperties typeId=" +
	 * typeId); objectInfo.setBaseType(BaseTypeId.CMIS_DOCUMENT);
	 * objectInfo.setTypeId(typeId); objectInfo.setHasAcl(true);
	 * objectInfo.setHasContent(true); objectInfo.setHasParent(true);
	 * objectInfo.setVersionSeriesId(null);
	 * objectInfo.setIsCurrentVersion(true);
	 * objectInfo.setRelationshipSourceIds(null);
	 * objectInfo.setRelationshipTargetIds(null);
	 * objectInfo.setRenditionInfos(null);
	 * objectInfo.setSupportsDescendants(false);
	 * objectInfo.setSupportsFolderTree(false);
	 * objectInfo.setSupportsPolicies(false);
	 * objectInfo.setSupportsRelationships(false);
	 * objectInfo.setWorkingCopyId(null);
	 * objectInfo.setWorkingCopyOriginalId(null); }
	 * 
	 * // let's do it try { PropertiesImpl result = new PropertiesImpl();
	 * 
	 * // id String id = fileToId(file); addPropertyId(result, typeId, filter,
	 * PropertyIds.OBJECT_ID, id); objectInfo.setId(id);
	 * debug("compileProperties objectinfo.id=" + objectInfo.getId());
	 * 
	 * // name String name = file.getName(); addPropertyString(result, typeId,
	 * filter, PropertyIds.NAME, name); objectInfo.setName(name);
	 * debug("compileProperties objectinfo.name=" + objectInfo.getName());
	 * 
	 * // created and modified by addPropertyString(result, typeId, filter,
	 * PropertyIds.CREATED_BY, USER_UNKNOWN); addPropertyString(result, typeId,
	 * filter, PropertyIds.LAST_MODIFIED_BY, USER_UNKNOWN);
	 * objectInfo.setCreatedBy(USER_UNKNOWN);
	 * 
	 * // creation and modification date GregorianCalendar lastModified =
	 * CouchbaseUtils .millisToCalendar(file.lastModified());
	 * addPropertyDateTime(result, typeId, filter, PropertyIds.CREATION_DATE,
	 * lastModified); addPropertyDateTime(result, typeId, filter,
	 * PropertyIds.LAST_MODIFICATION_DATE, lastModified);
	 * objectInfo.setCreationDate(lastModified);
	 * objectInfo.setLastModificationDate(lastModified);
	 * 
	 * // change token - always null addPropertyString(result, typeId, filter,
	 * PropertyIds.CHANGE_TOKEN, null);
	 * 
	 * // CMIS 1.1 properties if (context.getCmisVersion() !=
	 * CmisVersion.CMIS_1_0) { addPropertyString(result, typeId, filter,
	 * PropertyIds.DESCRIPTION, null); addPropertyIdList(result, typeId, filter,
	 * PropertyIds.SECONDARY_OBJECT_TYPE_IDS, null); }
	 * 
	 * // directory or file if (file.isDirectory()) {
	 * debug("compileProperties isDirectory");
	 * 
	 * // base type and type name addPropertyId(result, typeId, filter,
	 * PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
	 * addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID,
	 * BaseTypeId.CMIS_FOLDER.value()); String path = getRepositoryPath(file);
	 * addPropertyString(result, typeId, filter, PropertyIds.PATH, path);
	 * debug("compileProperties repopath=" + path);
	 * 
	 * // folder properties if (!root.equals(file)) {
	 * 
	 * String parent = root.equals(file.getParentFile()) ? ROOT_ID :
	 * fileToId(file.getParentFile()); addPropertyId(result, typeId, filter,
	 * PropertyIds.PARENT_ID, parent); objectInfo.setHasParent(true);
	 * debug("compileProperties parent=" + parent);
	 * 
	 * } else { addPropertyId(result, typeId, filter, PropertyIds.PARENT_ID,
	 * null); objectInfo.setHasParent(false); }
	 * debug("compileProperties hasParent=" + objectInfo.hasParent());
	 * 
	 * addPropertyIdList(result, typeId, filter,
	 * PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, null); } else {
	 * debug("compileProperties isDocument");
	 * 
	 * // base type and type name addPropertyId(result, typeId, filter,
	 * PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
	 * addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID,
	 * BaseTypeId.CMIS_DOCUMENT.value());
	 * 
	 * // file properties addPropertyBoolean(result, typeId, filter,
	 * PropertyIds.IS_IMMUTABLE, false); addPropertyBoolean(result, typeId,
	 * filter, PropertyIds.IS_LATEST_VERSION, true); addPropertyBoolean(result,
	 * typeId, filter, PropertyIds.IS_MAJOR_VERSION, true);
	 * addPropertyBoolean(result, typeId, filter,
	 * PropertyIds.IS_LATEST_MAJOR_VERSION, true); addPropertyString(result,
	 * typeId, filter, PropertyIds.VERSION_LABEL, file.getName());
	 * addPropertyId(result, typeId, filter, PropertyIds.VERSION_SERIES_ID,
	 * fileToId(file)); addPropertyBoolean(result, typeId, filter,
	 * PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false);
	 * addPropertyString(result, typeId, filter,
	 * PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, null);
	 * addPropertyString(result, typeId, filter,
	 * PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, null);
	 * addPropertyString(result, typeId, filter, PropertyIds.CHECKIN_COMMENT,
	 * ""); if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
	 * addPropertyBoolean(result, typeId, filter,
	 * PropertyIds.IS_PRIVATE_WORKING_COPY, false); }
	 * 
	 * if (file.length() == 0) { addPropertyBigInteger(result, typeId, filter,
	 * PropertyIds.CONTENT_STREAM_LENGTH, null); addPropertyString(result,
	 * typeId, filter, PropertyIds.CONTENT_STREAM_MIME_TYPE, null);
	 * addPropertyString(result, typeId, filter,
	 * PropertyIds.CONTENT_STREAM_FILE_NAME, null);
	 * 
	 * objectInfo.setHasContent(false); objectInfo.setContentType(null);
	 * objectInfo.setFileName(null); } else { addPropertyInteger(result, typeId,
	 * filter, PropertyIds.CONTENT_STREAM_LENGTH, file.length());
	 * addPropertyString(result, typeId, filter,
	 * PropertyIds.CONTENT_STREAM_MIME_TYPE, MimeTypes.getMIMEType(file));
	 * addPropertyString(result, typeId, filter,
	 * PropertyIds.CONTENT_STREAM_FILE_NAME, file.getName());
	 * 
	 * objectInfo.setHasContent(true);
	 * objectInfo.setContentType(MimeTypes.getMIMEType(file));
	 * objectInfo.setFileName(file.getName()); }
	 * 
	 * addPropertyId(result, typeId, filter, PropertyIds.CONTENT_STREAM_ID,
	 * null); }
	 * 
	 * if (objectInfo.getObject() != null) { Properties oldProp =
	 * objectInfo.getObject().getProperties(); Map<String, PropertyData<?>>
	 * oldMap = oldProp.getProperties();
	 * debug(" ==== OLD PROPERTIES of ENTRY ===="); for (Map.Entry<String,
	 * PropertyData<?>> entry : oldMap .entrySet()) { debug("entry.key : " +
	 * entry.getKey() + " - entry.value : " + entry.getValue()); } } else {
	 * debug("Old objectinfo.object is null"); }
	 * 
	 * // read custom properties debug("readCustomProperties ...");
	 * readCustomProperties(file, result, filter, objectInfo);
	 * debug("readCustomProperties done.");
	 * 
	 * if (objectInfo.getObject() != null) { Properties newProp =
	 * objectInfo.getObject().getProperties(); Map<String, PropertyData<?>>
	 * newMap = newProp.getProperties();
	 * debug(" ==== NEW PROPERTIES of ENTRY ===="); for (Map.Entry<String,
	 * PropertyData<?>> entry : newMap .entrySet()) { debug("entry.key : " +
	 * entry.getKey() + " - entry.value : " + entry.getValue()); } } else {
	 * debug("New objectinfo.object is null"); }
	 * 
	 * if (filter != null) { if (!filter.isEmpty()) {
	 * debug("Unknown filter properties: " + filter.toString()); } }
	 * 
	 * debug("objectInfo.getId : " + objectInfo.getId());
	 * debug("objectInfo.getAtomId : " + objectInfo.getAtomId());
	 * debug("objectInfo.getName : " + objectInfo.getName());
	 * debug("objectInfo.getCreatedBy : " + objectInfo.getCreatedBy());
	 * debug("objectInfo.getCreationDate : " + objectInfo.getCreationDate());
	 * debug("objectInfo.getLastModificationDate : " +
	 * objectInfo.getLastModificationDate()); debug("objectInfo.getBaseType : "
	 * + objectInfo.getBaseType()); debug("objectInfo.isCurrentVersion : " +
	 * objectInfo.isCurrentVersion()); debug("objectInfo.getVersionSeriesId : "
	 * + objectInfo.getVersionSeriesId());
	 * debug("objectInfo.getWorkingCopyId : " + objectInfo.getWorkingCopyId());
	 * debug("objectInfo.getWorkingCopyOriginalId : " +
	 * objectInfo.getWorkingCopyOriginalId()); debug("objectInfo.hasContent : "
	 * + objectInfo.hasContent()); debug("objectInfo.getContentType : " +
	 * objectInfo.getContentType()); debug("objectInfo.getFileName : " +
	 * objectInfo.getFileName()); debug("objectInfo.getRenditionInfos : " +
	 * objectInfo.getRenditionInfos());
	 * debug("objectInfo.supportsRelationships : " +
	 * objectInfo.supportsRelationships());
	 * debug("objectInfo.supportsPolicies : " + objectInfo.supportsPolicies());
	 * debug("objectInfo.hasAcl : " + objectInfo.hasAcl());
	 * debug("objectInfo.hasParent : " + objectInfo.hasParent());
	 * debug("objectInfo.supportsDescendants : " +
	 * objectInfo.supportsDescendants());
	 * debug("objectInfo.supportsFolderTree : " +
	 * objectInfo.supportsFolderTree());
	 * debug("objectInfo.getRelationshipSourceIds : " +
	 * objectInfo.getRelationshipSourceIds());
	 * debug("objectInfo.getRelationshipTargetIds : " +
	 * objectInfo.getRelationshipTargetIds());
	 * debug("objectInfo.getAdditionalLinks : " +
	 * objectInfo.getAdditionalLinks()); debug("objectInfo.getObject : " +
	 * objectInfo.getObject());
	 * 
	 * debug("compileProperties done.");
	 * 
	 * return result; } catch (CmisBaseException cbe) { throw cbe; } catch
	 * (Exception e) { throw new CmisRuntimeException(e.getMessage(), e); } }
	 */

	/**
	 * Reads and adds properties.
	 */
	private void readCustomProperties(File file, PropertiesImpl properties,
			Set<String> filter, ObjectInfoImpl objectInfo) {
		File propFile = getPropertiesFile(file);

		// if it doesn't exists, ignore it
		if (!propFile.exists()) {
			return;
		}

		// parse it
		ObjectData obj = null;
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(propFile),
					64 * 1024);
			XMLStreamReader parser = XMLUtils.createParser(stream);
			XMLUtils.findNextStartElemenet(parser);
			obj = XMLConverter.convertObject(parser);
			parser.close();
		} catch (Exception e) {
			LOG.warn("Unvalid CMIS properties: {}", propFile.getAbsolutePath(),
					e);
		} finally {
			IOUtils.closeQuietly(stream);
		}

		if (obj == null || obj.getProperties() == null) {
			return;
		}

		// add it to properties
		for (PropertyData<?> prop : obj.getProperties().getPropertyList()) {
			// overwrite object info
			if (prop instanceof PropertyString) {
				String firstValueStr = ((PropertyString) prop).getFirstValue();
				if (PropertyIds.NAME.equals(prop.getId())) {
					objectInfo.setName(firstValueStr);
				} else if (PropertyIds.OBJECT_TYPE_ID.equals(prop.getId())) {
					objectInfo.setTypeId(firstValueStr);
				} else if (PropertyIds.CREATED_BY.equals(prop.getId())) {
					objectInfo.setCreatedBy(firstValueStr);
				} else if (PropertyIds.CONTENT_STREAM_MIME_TYPE.equals(prop
						.getId())) {
					objectInfo.setContentType(firstValueStr);
				} else if (PropertyIds.CONTENT_STREAM_FILE_NAME.equals(prop
						.getId())) {
					objectInfo.setFileName(firstValueStr);
				}
			}

			if (prop instanceof PropertyDateTime) {
				GregorianCalendar firstValueCal = ((PropertyDateTime) prop)
						.getFirstValue();
				if (PropertyIds.CREATION_DATE.equals(prop.getId())) {
					objectInfo.setCreationDate(firstValueCal);
				} else if (PropertyIds.LAST_MODIFICATION_DATE.equals(prop
						.getId())) {
					objectInfo.setLastModificationDate(firstValueCal);
				}
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
			if (PropertyIds.OBJECT_ID.equals(prop.getId())) {
				continue;
			}

			// don't overwrite base type
			if (PropertyIds.BASE_TYPE_ID.equals(prop.getId())) {
				continue;
			}

			// add it
			debug("Replacing property : " + prop);
			properties.replaceProperty(prop);
		}

		debug("========= //\nProperties : " + properties);
		debug("========= //\nObjectInfo.typeId : " + objectInfo.getTypeId());

	}

	/*
	 * private void readCustomProperties(File file, PropertiesImpl properties,
	 * Set<String> filter, ObjectInfoImpl objectInfo) { File propFile =
	 * getPropertiesFile(file);
	 * 
	 * // if it doesn't exists, ignore it if (!propFile.exists()) { return; }
	 * 
	 * // parse it ObjectData obj = null; InputStream stream = null; try {
	 * stream = new BufferedInputStream(new FileInputStream(propFile), 64 *
	 * 1024); XMLStreamReader parser = XMLUtils.createParser(stream);
	 * XMLUtils.findNextStartElemenet(parser); obj =
	 * XMLConverter.convertObject(parser); parser.close(); } catch (Exception e)
	 * { LOG.warn("Unvalid CMIS properties: {}", propFile.getAbsolutePath(), e);
	 * } finally { IOUtils.closeQuietly(stream); }
	 * 
	 * if (obj == null || obj.getProperties() == null) { return; }
	 * 
	 * // add it to properties for (PropertyData<?> prop :
	 * obj.getProperties().getPropertyList()) { // overwrite object info if
	 * (prop instanceof PropertyString) { String firstValueStr =
	 * ((PropertyString) prop).getFirstValue(); if
	 * (PropertyIds.NAME.equals(prop.getId())) {
	 * objectInfo.setName(firstValueStr); } else if
	 * (PropertyIds.OBJECT_TYPE_ID.equals(prop.getId())) {
	 * objectInfo.setTypeId(firstValueStr); } else if
	 * (PropertyIds.CREATED_BY.equals(prop.getId())) {
	 * objectInfo.setCreatedBy(firstValueStr); } else if
	 * (PropertyIds.CONTENT_STREAM_MIME_TYPE.equals(prop .getId())) {
	 * objectInfo.setContentType(firstValueStr); } else if
	 * (PropertyIds.CONTENT_STREAM_FILE_NAME.equals(prop .getId())) {
	 * objectInfo.setFileName(firstValueStr); } }
	 * 
	 * if (prop instanceof PropertyDateTime) { GregorianCalendar firstValueCal =
	 * ((PropertyDateTime) prop) .getFirstValue(); if
	 * (PropertyIds.CREATION_DATE.equals(prop.getId())) {
	 * objectInfo.setCreationDate(firstValueCal); } else if
	 * (PropertyIds.LAST_MODIFICATION_DATE.equals(prop .getId())) {
	 * objectInfo.setLastModificationDate(firstValueCal); } }
	 * 
	 * // check filter if (filter != null) { if
	 * (!filter.contains(prop.getQueryName())) { continue; } else {
	 * filter.remove(prop.getQueryName()); } }
	 * 
	 * // don't overwrite id if (PropertyIds.OBJECT_ID.equals(prop.getId())) {
	 * continue; }
	 * 
	 * // don't overwrite base type if
	 * (PropertyIds.BASE_TYPE_ID.equals(prop.getId())) { continue; }
	 * 
	 * // add it debug("Replacing property : " + prop);
	 * properties.replaceProperty(prop); }
	 * 
	 * debug("========= //\nProperties : " + properties);
	 * debug("========= //\nObjectInfo.typeId : " + objectInfo.getTypeId());
	 * 
	 * }
	 */

	/**
	 * Checks and compiles a property set that can be written to disc.
	 */
	private PropertiesImpl compileWriteProperties(String typeId,
			String creator, String modifier, Properties properties) {
		PropertiesImpl result = new PropertiesImpl();
		Set<String> addedProps = new HashSet<String>();

		if (properties == null || properties.getProperties() == null) {
			throw new CmisConstraintException("No properties!");
		}

		// get the property definitions
		TypeDefinition type = typeManager.getInternalTypeDefinition(typeId);
		if (type == null) {
			throw new CmisObjectNotFoundException("Type '" + typeId
					+ "' is unknown!");
		}

		// check if all required properties are there
		for (PropertyData<?> prop : properties.getProperties().values()) {
			PropertyDefinition<?> propType = type.getPropertyDefinitions().get(
					prop.getId());

			// do we know that property?
			if (propType == null) {
				throw new CmisConstraintException("Property '" + prop.getId()
						+ "' is unknown!");
			}

			// can it be set?
			if (propType.getUpdatability() == Updatability.READONLY) {
				throw new CmisConstraintException("Property '" + prop.getId()
						+ "' is readonly!");
			}

			// empty properties are invalid
			// TODO: check
			// if (isEmptyProperty(prop)) {
			// throw new CmisConstraintException("Property '" + prop.getId() +
			// "' must not be empty!");
			// }

			// add it
			result.addProperty(prop);
			addedProps.add(prop.getId());
		}

		// check if required properties are missing
		for (PropertyDefinition<?> propDef : type.getPropertyDefinitions()
				.values()) {
			if (!addedProps.contains(propDef.getId())
					&& propDef.getUpdatability() != Updatability.READONLY) {
				if (!addPropertyDefault(result, propDef)
						&& propDef.isRequired()) {
					throw new CmisConstraintException("Property '"
							+ propDef.getId() + "' is required!");
				}
			}
		}

		addPropertyId(result, typeId, null, PropertyIds.OBJECT_TYPE_ID, typeId);
		addPropertyString(result, typeId, null, PropertyIds.CREATED_BY, creator);
		addPropertyString(result, typeId, null, PropertyIds.LAST_MODIFIED_BY,
				modifier);

		return result;
	}

	/**
	 * Writes the properties for a document or folder.
	 */
	private void writePropertiesFile(File file, Properties properties) {
		/*
		 * File propFile = getPropertiesFile(file);
		 * 
		 * // if no properties set delete the properties file if (properties ==
		 * null || properties.getProperties() == null ||
		 * properties.getProperties().size() == 0) { propFile.delete(); return;
		 * }
		 * 
		 * // create object ObjectDataImpl object = new ObjectDataImpl();
		 * object.setProperties(properties);
		 * 
		 * OutputStream stream = null; try { stream = new
		 * BufferedOutputStream(new FileOutputStream(propFile)); XMLStreamWriter
		 * writer = XMLUtils.createWriter(stream);
		 * XMLUtils.startXmlDocument(writer); XMLConverter.writeObject(writer,
		 * CmisVersion.CMIS_1_1, true, "object", XMLConstants.NAMESPACE_CMIS,
		 * object); XMLUtils.endXmlDocument(writer); writer.close(); } catch
		 * (Exception e) { throw new
		 * CmisStorageException("Couldn't store properties!", e); } finally {
		 * IOUtils.closeQuietly(stream); }
		 */
		debug("writePropertiesFile not supported anymore.");
	}

	private boolean isEmptyProperty(PropertyData<?> prop) {
		if (prop == null || prop.getValues() == null) {
			return true;
		}

		return prop.getValues().isEmpty();
	}

	private void addPropertyId(PropertiesImpl props, String typeId,
			Set<String> filter, String id, String value) {
		if (!checkAddProperty(props, typeId, filter, id)) {
			return;
		}

		props.addProperty(new PropertyIdImpl(id, value));
	}

	private void addPropertyIdList(PropertiesImpl props, String typeId,
			Set<String> filter, String id, List<String> value) {
		if (!checkAddProperty(props, typeId, filter, id)) {
			return;
		}

		props.addProperty(new PropertyIdImpl(id, value));
	}

	private void addPropertyString(PropertiesImpl props, String typeId,
			Set<String> filter, String id, String value) {
		if (!checkAddProperty(props, typeId, filter, id)) {
			return;
		}

		props.addProperty(new PropertyStringImpl(id, value));
	}

	private void addPropertyInteger(PropertiesImpl props, String typeId,
			Set<String> filter, String id, long value) {
		addPropertyBigInteger(props, typeId, filter, id,
				BigInteger.valueOf(value));
	}

	private void addPropertyBigInteger(PropertiesImpl props, String typeId,
			Set<String> filter, String id, BigInteger value) {
		if (!checkAddProperty(props, typeId, filter, id)) {
			return;
		}

		props.addProperty(new PropertyIntegerImpl(id, value));
	}

	private void addPropertyBoolean(PropertiesImpl props, String typeId,
			Set<String> filter, String id, boolean value) {
		if (!checkAddProperty(props, typeId, filter, id)) {
			return;
		}

		props.addProperty(new PropertyBooleanImpl(id, value));
	}

	private void addPropertyDateTime(PropertiesImpl props, String typeId,
			Set<String> filter, String id, GregorianCalendar value) {
		if (!checkAddProperty(props, typeId, filter, id)) {
			return;
		}

		props.addProperty(new PropertyDateTimeImpl(id, value));
	}

	private boolean checkAddProperty(Properties properties, String typeId,
			Set<String> filter, String id) {
		if (properties == null || properties.getProperties() == null) {
			throw new IllegalArgumentException("Properties must not be null!");
		}

		if (id == null) {
			throw new IllegalArgumentException("Id must not be null!");
		}

		TypeDefinition type = typeManager.getInternalTypeDefinition(typeId);
		if (type == null) {
			throw new IllegalArgumentException("Unknown type: " + typeId);
		}
		if (!type.getPropertyDefinitions().containsKey(id)) {
			throw new IllegalArgumentException("Unknown property: " + id);
		}

		String queryName = type.getPropertyDefinitions().get(id).getQueryName();

		if (queryName != null && filter != null) {
			if (!filter.contains(queryName)) {
				return false;
			} else {
				filter.remove(queryName);
			}
		}

		return true;
	}

	/**
	 * Adds the default value of property if defined.
	 */
	@SuppressWarnings("unchecked")
	private boolean addPropertyDefault(PropertiesImpl props,
			PropertyDefinition<?> propDef) {
		if (props == null || props.getProperties() == null) {
			throw new IllegalArgumentException("Props must not be null!");
		}

		if (propDef == null) {
			return false;
		}

		List<?> defaultValue = propDef.getDefaultValue();
		if (defaultValue != null && !defaultValue.isEmpty()) {
			switch (propDef.getPropertyType()) {
			case BOOLEAN:
				props.addProperty(new PropertyBooleanImpl(propDef.getId(),
						(List<Boolean>) defaultValue));
				break;
			case DATETIME:
				props.addProperty(new PropertyDateTimeImpl(propDef.getId(),
						(List<GregorianCalendar>) defaultValue));
				break;
			case DECIMAL:
				props.addProperty(new PropertyDecimalImpl(propDef.getId(),
						(List<BigDecimal>) defaultValue));
				break;
			case HTML:
				props.addProperty(new PropertyHtmlImpl(propDef.getId(),
						(List<String>) defaultValue));
				break;
			case ID:
				props.addProperty(new PropertyIdImpl(propDef.getId(),
						(List<String>) defaultValue));
				break;
			case INTEGER:
				props.addProperty(new PropertyIntegerImpl(propDef.getId(),
						(List<BigInteger>) defaultValue));
				break;
			case STRING:
				props.addProperty(new PropertyStringImpl(propDef.getId(),
						(List<String>) defaultValue));
				break;
			case URI:
				props.addProperty(new PropertyUriImpl(propDef.getId(),
						(List<String>) defaultValue));
				break;
			default:
				assert false;
			}

			return true;
		}

		return false;
	}

	/**
	 * Compiles the allowable actions for a file or folder.
	 */
	private AllowableActions compileAllowableActions(File file,
			boolean userReadOnly) {
		debug("compileAllowableActions not yet implemented");
		/*
		 * if (file == null) { throw new
		 * IllegalArgumentException("File must not be null!"); }
		 * 
		 * // we can't gather allowable actions if the file or folder doesn't
		 * exist if (!file.exists()) { throw new
		 * CmisObjectNotFoundException("Object not found!"); }
		 * 
		 * boolean isReadOnly = !file.canWrite(); boolean isFolder =
		 * file.isDirectory(); boolean isRoot = root.equals(file);
		 * 
		 * Set<Action> aas = EnumSet.noneOf(Action.class);
		 * 
		 * addAction(aas, Action.CAN_GET_OBJECT_PARENTS, !isRoot);
		 * addAction(aas, Action.CAN_GET_PROPERTIES, true); addAction(aas,
		 * Action.CAN_UPDATE_PROPERTIES, !userReadOnly && !isReadOnly);
		 * addAction(aas, Action.CAN_MOVE_OBJECT, !userReadOnly && !isRoot);
		 * addAction(aas, Action.CAN_DELETE_OBJECT, !userReadOnly && !isReadOnly
		 * && !isRoot); addAction(aas, Action.CAN_GET_ACL, true);
		 * 
		 * if (isFolder) { addAction(aas, Action.CAN_GET_DESCENDANTS, true);
		 * addAction(aas, Action.CAN_GET_CHILDREN, true); addAction(aas,
		 * Action.CAN_GET_FOLDER_PARENT, !isRoot); addAction(aas,
		 * Action.CAN_GET_FOLDER_TREE, true); addAction(aas,
		 * Action.CAN_CREATE_DOCUMENT, !userReadOnly); addAction(aas,
		 * Action.CAN_CREATE_FOLDER, !userReadOnly); addAction(aas,
		 * Action.CAN_DELETE_TREE, !userReadOnly && !isReadOnly); } else {
		 * addAction(aas, Action.CAN_GET_CONTENT_STREAM, file.length() > 0);
		 * addAction(aas, Action.CAN_SET_CONTENT_STREAM, !userReadOnly &&
		 * !isReadOnly); addAction(aas, Action.CAN_DELETE_CONTENT_STREAM,
		 * !userReadOnly && !isReadOnly); addAction(aas,
		 * Action.CAN_GET_ALL_VERSIONS, true); }
		 * 
		 * AllowableActionsImpl result = new AllowableActionsImpl();
		 * result.setAllowableActions(aas);
		 * 
		 * return result;
		 */
		return null;
	}

	private void addAction(Set<Action> aas, Action action, boolean condition) {
		if (condition) {
			aas.add(action);
		}
	}

	/**
	 * Compiles the ACL for a file or folder.
	 */
	private Acl compileAcl(File file) {
		AccessControlListImpl result = new AccessControlListImpl();
		result.setAces(new ArrayList<Ace>());

		for (Map.Entry<String, Boolean> ue : readWriteUserMap.entrySet()) {
			// create principal
			AccessControlPrincipalDataImpl principal = new AccessControlPrincipalDataImpl(
					ue.getKey());

			// create ACE
			AccessControlEntryImpl entry = new AccessControlEntryImpl();
			entry.setPrincipal(principal);
			entry.setPermissions(new ArrayList<String>());
			entry.getPermissions().add(BasicPermissions.READ);
			if (!ue.getValue().booleanValue() && file.canWrite()) {
				entry.getPermissions().add(BasicPermissions.WRITE);
				entry.getPermissions().add(BasicPermissions.ALL);
			}

			entry.setDirect(true);

			// add ACE
			result.getAces().add(entry);
		}

		return result;
	}

	/**
	 * Checks if the given name is valid for a file system.
	 * 
	 * @param name
	 *            the name to check
	 * 
	 * @return <code>true</code> if the name is valid, <code>false</code>
	 *         otherwise
	 */
	private boolean isValidName(String name) {
		if (name == null || name.length() == 0
				|| name.indexOf(File.separatorChar) != -1
				|| name.indexOf(File.pathSeparatorChar) != -1) {
			return false;
		}

		return true;
	}

	/**
	 * Checks if a folder is empty. A folder is considered as empty if no files
	 * or only the shadow file reside in the folder.
	 * 
	 * @param folder
	 *            the folder
	 * 
	 * @return <code>true</code> if the folder is empty.
	 */
	private boolean isFolderEmpty(File folder) {
		if (!folder.isDirectory()) {
			return true;
		}

		String[] fileNames = folder.list();

		if (fileNames == null || fileNames.length == 0) {
			return true;
		}

		if (fileNames.length == 1 && fileNames[0].equals(SHADOW_FOLDER)) {
			return true;
		}

		return false;
	}

	/**
	 * Checks if the user in the given context is valid for this repository and
	 * if the user has the required permissions.
	 */
	private boolean checkUser(CallContext context, boolean writeRequired) {
		if (context == null) {
			throw new CmisPermissionDeniedException("No user context!");
		}

		Boolean readOnly = readWriteUserMap.get(context.getUsername());
		if (readOnly == null) {
			throw new CmisPermissionDeniedException("Unknown user!");
		}

		if (readOnly.booleanValue() && writeRequired) {
			throw new CmisPermissionDeniedException("No write permission!");
		}

		return readOnly.booleanValue();
	}

	/**
	 * Returns the properties file of the given file.
	 */
	private File getPropertiesFile(File file) {
		if (file.isDirectory()) {
			return new File(file, SHADOW_FOLDER);
		}

		return new File(file.getAbsolutePath() + SHADOW_EXT);
	}

	/**
	 * Returns the File object by id or throws an appropriate exception.
	 */
	/*
	 * private File getFile(String id) { try { return idToFile(id); } catch
	 * (Exception e) { throw new CmisObjectNotFoundException(e.getMessage(), e);
	 * } }
	 */

	/**
	 * Converts an id to a File object. A simple and insecure implementation,
	 * but good enough for now.
	 */
	/*
	 * private File idToFile(String id) throws IOException { if (id == null ||
	 * id.length() == 0) { throw new
	 * CmisInvalidArgumentException("Id is not valid!"); }
	 * 
	 * if (id.equals(ROOT_ID)) { return root; }
	 * 
	 * return new File(root, (new String(
	 * Base64.decode(id.getBytes("US-ASCII")), "UTF-8")).replace('/',
	 * File.separatorChar)); }
	 */

	/**
	 * Returns the id of a File object or throws an appropriate exception.
	 */
	/*
	 * private String getId(File file) { try { return fileToId(file); } catch
	 * (Exception e) { throw new CmisRuntimeException(e.getMessage(), e); } }
	 */

	/**
	 * Creates a File object from an id. A simple and insecure implementation,
	 * but good enough for now.
	 */
	/*
	 * private String fileToId(File file) throws IOException { if (file == null)
	 * { throw new IllegalArgumentException("File is not valid!"); }
	 * 
	 * if (root.equals(file)) { return ROOT_ID; }
	 * 
	 * String path = getRepositoryPath(file);
	 * 
	 * return Base64.encodeBytes(path.getBytes("UTF-8")); }
	 */

	/*
	 * private String getRepositoryPath(File file) { String path =
	 * file.getAbsolutePath() .substring(root.getAbsolutePath().length())
	 * .replace(File.separatorChar, '/'); if (path.length() == 0) { path = "/";
	 * } else if (path.charAt(0) != '/') { path = "/" + path; } return path; }
	 */

	private void debug(String msg) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("<{}> {}", repositoryId, msg);
		}
		// System.out.println("(" + repositoryId + ") {" + msg + "}");
	}
}
