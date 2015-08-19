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
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ObjectInFolderContainerImpl;
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

import com.couchbase.client.java.document.JsonDocument;


public class CouchbasePropertiesManager{
	
	private static final String ROOT_ID = "@root@";
	private static final String SHADOW_EXT = ".cmis.xml";
	private static final String SHADOW_FOLDER = "cmis.xml";

	private static final String USER_UNKNOWN = "<unknown>";
	
	private final CouchbaseTypeManager typeManager;
	 
	public CouchbasePropertiesManager(CouchbaseTypeManager typeManager){
		this.typeManager = typeManager;
	}

	/*public boolean checkAddProperty(Properties properties, String typeId, Set<String> filter, String id) {
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
    }*/
	
	 /**
     * Writes the properties for a document or folder.
     */
    public void writePropertiesFile(File file, Properties properties) {
        File propFile = getPropertiesFile(file);

        // if no properties set delete the properties file
        if (properties == null || properties.getProperties() == null || properties.getProperties().size() == 0) {
            propFile.delete();
            return;
        }

        // create object
        ObjectDataImpl object = new ObjectDataImpl();
        object.setProperties(properties);

        OutputStream stream = null;
        try {
            stream = new BufferedOutputStream(new FileOutputStream(propFile));
            XMLStreamWriter writer = XMLUtils.createWriter(stream);
            XMLUtils.startXmlDocument(writer);
            XMLConverter.writeObject(writer, CmisVersion.CMIS_1_1, true, "object", XMLConstants.NAMESPACE_CMIS, object);
            XMLUtils.endXmlDocument(writer);
            writer.close();
        } catch (Exception e) {
            throw new CmisStorageException("Couldn't store properties!", e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        
        //couchbase
        try{
        	//String docId = properties.getProperties().get(PropertyIds.OBJECT_ID).toString();
        	String docId = System.currentTimeMillis()+"";
        	JsonDocument jsondoc = CouchbaseService.getInstance().createDocumentProperties(docId, properties);
        	// TODO
        	
        	System.out.println("Properties to Couchbase : docId="+docId+" - jsondoc="+jsondoc);
             	
        }
        catch(Exception e){
        	e.printStackTrace();
        	System.out.println("Cannot write Properties to Couchbase");
        }
    }
    
    /*public boolean isEmptyProperty(PropertyData<?> prop) {
        if (prop == null || prop.getValues() == null) {
            return true;
        }

        return prop.getValues().isEmpty();
    }*/

   /* public void addPropertyId(PropertiesImpl props, String typeId, Set<String> filter, String id, String value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyIdImpl(id, value));
    }*/

    /*public void addPropertyIdList(PropertiesImpl props, String typeId, Set<String> filter, String id,
            List<String> value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyIdImpl(id, value));
    }*/

    /*public void addPropertyString(PropertiesImpl props, String typeId, Set<String> filter, String id, String value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyStringImpl(id, value));
    }*/

    /*public void addPropertyInteger(PropertiesImpl props, String typeId, Set<String> filter, String id, long value) {
        addPropertyBigInteger(props, typeId, filter, id, BigInteger.valueOf(value));
    }*/

    /*public void addPropertyBigInteger(PropertiesImpl props, String typeId, Set<String> filter, String id,
            BigInteger value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyIntegerImpl(id, value));
    }*/

    /*public void addPropertyBoolean(PropertiesImpl props, String typeId, Set<String> filter, String id, boolean value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyBooleanImpl(id, value));
    }*/

    /*public void addPropertyDateTime(PropertiesImpl props, String typeId, Set<String> filter, String id,
            GregorianCalendar value) {
        if (!checkAddProperty(props, typeId, filter, id)) {
            return;
        }

        props.addProperty(new PropertyDateTimeImpl(id, value));
    }*/
    

    /**
     * Adds the default value of property if defined.
     */
    /*@SuppressWarnings("unchecked")
    public boolean addPropertyDefault(PropertiesImpl props, PropertyDefinition<?> propDef) {
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
                props.addProperty(new PropertyBooleanImpl(propDef.getId(), (List<Boolean>) defaultValue));
                break;
            case DATETIME:
                props.addProperty(new PropertyDateTimeImpl(propDef.getId(), (List<GregorianCalendar>) defaultValue));
                break;
            case DECIMAL:
                props.addProperty(new PropertyDecimalImpl(propDef.getId(), (List<BigDecimal>) defaultValue));
                break;
            case HTML:
                props.addProperty(new PropertyHtmlImpl(propDef.getId(), (List<String>) defaultValue));
                break;
            case ID:
                props.addProperty(new PropertyIdImpl(propDef.getId(), (List<String>) defaultValue));
                break;
            case INTEGER:
                props.addProperty(new PropertyIntegerImpl(propDef.getId(), (List<BigInteger>) defaultValue));
                break;
            case STRING:
                props.addProperty(new PropertyStringImpl(propDef.getId(), (List<String>) defaultValue));
                break;
            case URI:
                props.addProperty(new PropertyUriImpl(propDef.getId(), (List<String>) defaultValue));
                break;
            default:
                assert false;
            }

            return true;
        }

        return false;
    }*/
    
    /**
     * Returns the properties file of the given file.
     */
    public File getPropertiesFile(File file) {
        if (file.isDirectory()) {
            return new File(file, SHADOW_FOLDER);
        }

        return new File(file.getAbsolutePath() + SHADOW_EXT);
    }
    
    /**
     * Gathers all base properties of a file or folder.
     */
    /*public Properties compileProperties(CallContext context, File file, Set<String> orgfilter,
            ObjectInfoImpl objectInfo) {
        if (file == null) {
            throw new IllegalArgumentException("File must not be null!");
        }

        // we can't gather properties if the file or folder doesn't exist
        if (!file.exists()) {
            throw new CmisObjectNotFoundException("Object not found!");
        }

        // copy filter
        Set<String> filter = (orgfilter == null ? null : new HashSet<String>(orgfilter));

        // find base type
        String typeId = null;

        if (file.isDirectory()) {
            typeId = BaseTypeId.CMIS_FOLDER.value();
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
            String id = fileToId(file);
            addPropertyId(result, typeId, filter, PropertyIds.OBJECT_ID, id);
            objectInfo.setId(id);

            // name
            String name = file.getName();
            addPropertyString(result, typeId, filter, PropertyIds.NAME, name);
            objectInfo.setName(name);

            // created and modified by
            addPropertyString(result, typeId, filter, PropertyIds.CREATED_BY, USER_UNKNOWN);
            addPropertyString(result, typeId, filter, PropertyIds.LAST_MODIFIED_BY, USER_UNKNOWN);
            objectInfo.setCreatedBy(USER_UNKNOWN);

            // creation and modification date
            GregorianCalendar lastModified = CouchbaseUtils.millisToCalendar(file.lastModified());
            addPropertyDateTime(result, typeId, filter, PropertyIds.CREATION_DATE, lastModified);
            addPropertyDateTime(result, typeId, filter, PropertyIds.LAST_MODIFICATION_DATE, lastModified);
            objectInfo.setCreationDate(lastModified);
            objectInfo.setLastModificationDate(lastModified);

            // change token - always null
            addPropertyString(result, typeId, filter, PropertyIds.CHANGE_TOKEN, null);

            // CMIS 1.1 properties
            if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
                addPropertyString(result, typeId, filter, PropertyIds.DESCRIPTION, null);
                addPropertyIdList(result, typeId, filter, PropertyIds.SECONDARY_OBJECT_TYPE_IDS, null);
            }

            // directory or file
            if (file.isDirectory()) {
                // base type and type name
                addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
                addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_FOLDER.value());
                String path = getRepositoryPath(file);
                addPropertyString(result, typeId, filter, PropertyIds.PATH, path);

                // folder properties
                if (!root.equals(file)) {
                    addPropertyId(result, typeId, filter, PropertyIds.PARENT_ID,
                            (root.equals(file.getParentFile()) ? ROOT_ID : fileToId(file.getParentFile())));
                    objectInfo.setHasParent(true);
                } else {
                    addPropertyId(result, typeId, filter, PropertyIds.PARENT_ID, null);
                    objectInfo.setHasParent(false);
                }

                addPropertyIdList(result, typeId, filter, PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS, null);
            } else {
                // base type and type name
                addPropertyId(result, typeId, filter, PropertyIds.BASE_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());
                addPropertyId(result, typeId, filter, PropertyIds.OBJECT_TYPE_ID, BaseTypeId.CMIS_DOCUMENT.value());

                // file properties
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_IMMUTABLE, false);
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_LATEST_VERSION, true);
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_MAJOR_VERSION, true);
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_LATEST_MAJOR_VERSION, true);
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_LABEL, file.getName());
                addPropertyId(result, typeId, filter, PropertyIds.VERSION_SERIES_ID, fileToId(file));
                addPropertyBoolean(result, typeId, filter, PropertyIds.IS_VERSION_SERIES_CHECKED_OUT, false);
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_BY, null);
                addPropertyString(result, typeId, filter, PropertyIds.VERSION_SERIES_CHECKED_OUT_ID, null);
                addPropertyString(result, typeId, filter, PropertyIds.CHECKIN_COMMENT, "");
                if (context.getCmisVersion() != CmisVersion.CMIS_1_0) {
                    addPropertyBoolean(result, typeId, filter, PropertyIds.IS_public_WORKING_COPY, false);
                }

                if (file.length() == 0) {
                    addPropertyBigInteger(result, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, null);
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_MIME_TYPE, null);
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, null);

                    objectInfo.setHasContent(false);
                    objectInfo.setContentType(null);
                    objectInfo.setFileName(null);
                } else {
                    addPropertyInteger(result, typeId, filter, PropertyIds.CONTENT_STREAM_LENGTH, file.length());
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_MIME_TYPE,
                            MimeTypes.getMIMEType(file));
                    addPropertyString(result, typeId, filter, PropertyIds.CONTENT_STREAM_FILE_NAME, file.getName());

                    objectInfo.setHasContent(true);
                    objectInfo.setContentType(MimeTypes.getMIMEType(file));
                    objectInfo.setFileName(file.getName());
                }

                addPropertyId(result, typeId, filter, PropertyIds.CONTENT_STREAM_ID, null);
            }

            // read custom properties
            readCustomProperties(file, result, filter, objectInfo);

            if (filter != null) {
                if (!filter.isEmpty()) {
                    debug("Unknown filter properties: " + filter.toString());
                }
            }

            return result;
        } catch (CmisBaseException cbe) {
            throw cbe;
        } catch (Exception e) {
            throw new CmisRuntimeException(e.getMessage(), e);
        }
    }
*/
    /**
     * Reads and adds properties.
     */
    /*public void readCustomProperties(File file, PropertiesImpl properties, Set<String> filter,
            ObjectInfoImpl objectInfo) {
        File propFile = getPropertiesFile(file);

        // if it doesn't exists, ignore it
        if (!propFile.exists()) {
            return;
        }

        // parse it
        ObjectData obj = null;
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(new FileInputStream(propFile), 64 * 1024);
            XMLStreamReader parser = XMLUtils.createParser(stream);
            XMLUtils.findNextStartElemenet(parser);
            obj = XMLConverter.convertObject(parser);
            parser.close();
        } catch (Exception e) {
            LOG.warn("Unvalid CMIS properties: {}", propFile.getAbsolutePath(), e);
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
                } else if (PropertyIds.CONTENT_STREAM_MIME_TYPE.equals(prop.getId())) {
                    objectInfo.setContentType(firstValueStr);
                } else if (PropertyIds.CONTENT_STREAM_FILE_NAME.equals(prop.getId())) {
                    objectInfo.setFileName(firstValueStr);
                }
            }

            if (prop instanceof PropertyDateTime) {
                GregorianCalendar firstValueCal = ((PropertyDateTime) prop).getFirstValue();
                if (PropertyIds.CREATION_DATE.equals(prop.getId())) {
                    objectInfo.setCreationDate(firstValueCal);
                } else if (PropertyIds.LAST_MODIFICATION_DATE.equals(prop.getId())) {
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
            properties.replaceProperty(prop);
        }
    }*/

}