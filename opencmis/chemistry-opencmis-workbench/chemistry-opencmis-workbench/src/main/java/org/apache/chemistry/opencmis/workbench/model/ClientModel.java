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
package org.apache.chemistry.opencmis.workbench.model;

import static org.apache.chemistry.opencmis.commons.impl.CollectionsHelper.isNotEmpty;
import static org.apache.chemistry.opencmis.commons.impl.CollectionsHelper.isNullOrEmpty;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.EventListenerList;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.FileableCmisObject;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.QueryResult;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.client.runtime.OperationContextImpl;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.RepositoryCapabilities;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CapabilityChanges;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.impl.IOUtils;
import org.apache.chemistry.opencmis.commons.impl.MimeTypes;
import org.apache.chemistry.opencmis.workbench.LoggingInputStream;
import org.apache.chemistry.opencmis.workbench.RandomInputStream;

public class ClientModel {

    // object details must not be older than 60 seconds
    private static final long OLD = 60 * 1000;

    private ClientSession clientSession;

    private Folder currentFolder = null;
    private List<CmisObject> currentChildren = Collections.emptyList();
    private CmisObject currentObject = null;
    private List<ObjectType> baseTypes = null;

    private final EventListenerList listenerList = new EventListenerList();

    public ClientModel() {
    }

    public void addFolderListener(FolderListener listener) {
        listenerList.add(FolderListener.class, listener);
    }

    public void removeFolderListener(FolderListener listener) {
        listenerList.remove(FolderListener.class, listener);
    }

    public void addObjectListener(ObjectListener listener) {
        listenerList.add(ObjectListener.class, listener);
    }

    public void removeObjectListener(ObjectListener listener) {
        listenerList.remove(ObjectListener.class, listener);
    }

    public synchronized void setClientSession(ClientSession clientSession) {
        this.clientSession = clientSession;
        this.currentFolder = null;
        this.currentChildren = Collections.emptyList();
        this.currentObject = null;
        this.baseTypes = null;
    }

    public synchronized ClientSession getClientSession() {
        return clientSession;
    }

    public synchronized RepositoryInfo getRepositoryInfo() {
        Session session = clientSession.getSession();
        return session.getRepositoryInfo();
    }

    public synchronized String getRepositoryName() {
        try {
            return getRepositoryInfo().getName();
        } catch (Exception e) {
            return "?";
        }
    }

    public synchronized boolean supportsQuery() {
        try {
            RepositoryCapabilities cap = getRepositoryInfo().getCapabilities();
            if (cap == null) {
                return true;
            }

            return (cap.getQueryCapability() != null) && (cap.getQueryCapability() != CapabilityQuery.NONE);
        } catch (Exception e) {
            return false;
        }
    }

    public synchronized boolean supportsChangeLog() {
        try {
            RepositoryCapabilities cap = getRepositoryInfo().getCapabilities();
            if (cap == null) {
                return true;
            }

            return (cap.getChangesCapability() != null) && (cap.getChangesCapability() != CapabilityChanges.NONE);
        } catch (Exception e) {
            return false;
        }
    }

    private synchronized void loadBaseTypes() {
        if (baseTypes != null) {
            return;
        }

        baseTypes = new ArrayList<ObjectType>();
        for (ObjectType type : clientSession.getSession().getTypeChildren(null, false)) {
            baseTypes.add(type);
        }
    }

    public synchronized boolean supportsItems() {
        loadBaseTypes();

        for (ObjectType type : baseTypes) {
            if (type.getBaseTypeId() == BaseTypeId.CMIS_ITEM) {
                return true;
            }
        }

        return false;
    }

    public synchronized boolean supportsRelationships() {
        loadBaseTypes();

        for (ObjectType type : baseTypes) {
            if (type.getBaseTypeId() == BaseTypeId.CMIS_RELATIONSHIP) {
                return true;
            }
        }

        return false;
    }

    public synchronized boolean supportsPolicies() {
        loadBaseTypes();

        for (ObjectType type : baseTypes) {
            if (type.getBaseTypeId() == BaseTypeId.CMIS_POLICY) {
                return true;
            }
        }

        return false;
    }

    public synchronized ObjectId loadFolder(String folderId, boolean byPath) {
        try {
            Session session = clientSession.getSession();
            CmisObject selectedObject = null;
            CmisObject folderObject = null;

            if (byPath) {
                selectedObject = session.getObjectByPath(folderId);
            } else {
                selectedObject = session.getObject(folderId);
            }

            if (selectedObject instanceof Folder) {
                folderObject = selectedObject;
            } else {
                if (selectedObject instanceof FileableCmisObject) {
                    List<Folder> parents = ((FileableCmisObject) selectedObject).getParents();
                    if (isNotEmpty(parents)) {
                        folderObject = parents.get(0);
                    } else {
                        setCurrentFolder(null, new ArrayList<CmisObject>(0));
                        return selectedObject;
                    }
                } else {
                    setCurrentFolder(null, new ArrayList<CmisObject>(0));
                    return selectedObject;
                }
            }

            List<CmisObject> children = new ArrayList<CmisObject>();

            if (clientSession.getMaxChildren() != 0) {
                // if maxChildren == 0 don't call getChildren()
                ItemIterable<CmisObject> iter = ((Folder) folderObject).getChildren(clientSession
                        .getFolderOperationContext());

                if (clientSession.getMaxChildren() > 0) {
                    // if maxChildren > 0 restrict number of children
                    // otherwise load all
                    iter = iter.getPage(clientSession.getMaxChildren());
                }

                for (CmisObject child : iter) {
                    children.add(child);
                }
            }

            setCurrentFolder((Folder) folderObject, children);

            return selectedObject;
        } catch (CmisBaseException ex) {
            setCurrentFolder(null, new ArrayList<CmisObject>(0));
            throw ex;
        }
    }

    public synchronized void reloadFolder() {
        if (currentFolder != null) {
            loadFolder(currentFolder.getId(), false);
        }
    }

    public synchronized void loadObject(String objectId) {
        try {
            Session session = clientSession.getSession();
            CmisObject object = session.getObject(objectId, clientSession.getObjectOperationContext());
            object.refreshIfOld(OLD);

            setCurrentObject(object);
        } catch (CmisBaseException ex) {
            setCurrentObject(null);
            throw ex;
        }
    }

    public synchronized void reloadObject() {
        if (currentObject == null) {
            return;
        }

        try {
            Session session = clientSession.getSession();
            CmisObject object = session.getObject(currentObject, clientSession.getObjectOperationContext());
            object.refresh();

            setCurrentObject(object);
        } catch (CmisBaseException ex) {
            setCurrentObject(null);
            throw ex;
        }
    }

    public synchronized ItemIterable<QueryResult> query(String q, boolean searchAllVersions, int maxHits) {
        OperationContext queryContext = new OperationContextImpl(null, false, false, false, IncludeRelationships.NONE,
                null, false, null, false, maxHits > 0 ? maxHits : 1);

        Session session = clientSession.getSession();
        return session.query(q, searchAllVersions, queryContext);
    }

    public synchronized List<Tree<ObjectType>> getTypeDescendants() {
        Session session = clientSession.getSession();
        return session.getTypeDescendants(null, -1, true);
    }

    public ContentStream createContentStream(String filename) throws FileNotFoundException {
        ContentStream content = null;
        if ((filename != null) && (filename.length() > 0)) {
            File file = new File(filename);
            InputStream stream = new LoggingInputStream(new BufferedInputStream(new FileInputStream(file), 512 * 1024),
                    file.getName());

            content = clientSession.getSession().getObjectFactory()
                    .createContentStream(file.getName(), file.length(), MimeTypes.getMIMEType(file), stream);
        }

        return content;
    }

    public synchronized ObjectId createDocument(String name, String type, String filename,
            Map<String, Object> additionalProperties, VersioningState versioningState, boolean unfiled)
            throws FileNotFoundException {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, name);
        properties.put(PropertyIds.OBJECT_TYPE_ID, type);

        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }

        ContentStream content = createContentStream(filename);

        try {
            return clientSession.getSession().createDocument(properties, (unfiled ? null : currentFolder), content,
                    versioningState, null, null, null);
        } finally {
            IOUtils.closeQuietly(content);
        }
    }

    public ContentStream createContentStream(String name, long length, long seed) {
        return clientSession
                .getSession()
                .getObjectFactory()
                .createContentStream(name, length, "application/octet-stream",
                        new LoggingInputStream(new RandomInputStream(length, seed), name + " (random)"));
    }

    public synchronized ObjectId createDocument(String name, String type, Map<String, Object> additionalProperties,
            long length, long seed, VersioningState versioningState, boolean unfiled) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, name);
        properties.put(PropertyIds.OBJECT_TYPE_ID, type);

        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }

        ContentStream content = createContentStream(name, length, seed);
        try {
            return clientSession.getSession().createDocument(properties, (unfiled ? null : currentFolder), content,
                    versioningState, null, null, null);
        } finally {
            IOUtils.closeQuietly(content);
        }
    }

    public synchronized ObjectId createItem(String name, String type, Map<String, Object> additionalProperties,
            boolean unfiled) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, name);
        properties.put(PropertyIds.OBJECT_TYPE_ID, type);

        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }

        return clientSession.getSession().createItem(properties, (unfiled ? null : currentFolder), null, null, null);
    }

    public synchronized ObjectId createFolder(String name, String type, Map<String, Object> additionalProperties) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, name);
        properties.put(PropertyIds.OBJECT_TYPE_ID, type);

        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }

        return clientSession.getSession().createFolder(properties, currentFolder, null, null, null);
    }

    public synchronized ObjectId createRelationship(String name, String type, String sourceId, String targetId,
            Map<String, Object> additionalProperties) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, name);
        properties.put(PropertyIds.OBJECT_TYPE_ID, type);
        properties.put(PropertyIds.SOURCE_ID, sourceId);
        properties.put(PropertyIds.TARGET_ID, targetId);

        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }

        return clientSession.getSession().createRelationship(properties, null, null, null);
    }

    public synchronized ObjectId createPolicy(String name, String type, String policyText,
            Map<String, Object> additionalProperties, boolean unfiled) {
        Map<String, Object> properties = new HashMap<String, Object>();
        properties.put(PropertyIds.NAME, name);
        properties.put(PropertyIds.OBJECT_TYPE_ID, type);
        if (policyText != null && policyText.length() > 0) {
            properties.put(PropertyIds.POLICY_TEXT, policyText);
        }

        if (additionalProperties != null) {
            properties.putAll(additionalProperties);
        }

        return clientSession.getSession().createPolicy(properties, (unfiled ? null : currentFolder), null, null, null);
    }

    public synchronized List<ObjectType> getTypesAsList(String rootTypeId, boolean creatableOnly) {
        List<ObjectType> result = new ArrayList<ObjectType>();

        ObjectType rootType = null;
        try {
            rootType = clientSession.getSession().getTypeDefinition(rootTypeId);
        } catch (CmisBaseException e) {
            return result;
        }

        List<Tree<ObjectType>> types = clientSession.getSession().getTypeDescendants(rootTypeId, -1, true);
        addType(types, result, creatableOnly);

        if (creatableOnly) {
            boolean isCreatable = (rootType.isCreatable() == null ? true : rootType.isCreatable().booleanValue());
            if (isCreatable) {
                result.add(rootType);
            }
        } else {
            result.add(rootType);
        }

        Collections.sort(result, new Comparator<ObjectType>() {
            public int compare(ObjectType ot1, ObjectType ot2) {
                return ot1.getDisplayName().compareTo(ot2.getDisplayName());
            }
        });

        return result;
    }

    private void addType(List<Tree<ObjectType>> types, List<ObjectType> resultList, boolean creatableOnly) {
        assert types != null;
        assert resultList != null;

        for (Tree<ObjectType> tt : types) {
            if (tt.getItem() != null) {
                if (creatableOnly) {
                    boolean isCreatable = (tt.getItem().isCreatable() == null ? true : tt.getItem().isCreatable()
                            .booleanValue());
                    if (isCreatable) {
                        resultList.add(tt.getItem());
                    }
                } else {
                    resultList.add(tt.getItem());
                }

                addType(tt.getChildren(), resultList, creatableOnly);
            }
        }
    }

    public synchronized Folder getCurrentFolder() {
        return currentFolder;
    }

    public synchronized List<CmisObject> getCurrentChildren() {
        return currentChildren;
    }

    public synchronized CmisObject getFromCurrentChildren(String id) {
        if (isNullOrEmpty(currentChildren)) {
            return null;
        }

        for (CmisObject o : currentChildren) {
            if (o.getId().equals(id)) {
                return o;
            }
        }

        return null;
    }

    private synchronized void setCurrentFolder(Folder folder, List<CmisObject> children) {
        currentFolder = folder;
        currentChildren = children;

        for (FolderListener fl : listenerList.getListeners(FolderListener.class)) {
            fl.folderLoaded(new ClientModelEvent(this));
        }
    }

    public synchronized CmisObject getCurrentObject() {
        return currentObject;
    }

    private synchronized void setCurrentObject(CmisObject object) {
        currentObject = object;

        for (ObjectListener ol : listenerList.getListeners(ObjectListener.class)) {
            ol.objectLoaded(new ClientModelEvent(this));
        }
    }
}
