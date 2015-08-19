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
package org.apache.chemistry.opencmis.client.runtime;

import static org.apache.chemistry.opencmis.commons.impl.CollectionsHelper.isNotEmpty;
import static org.apache.chemistry.opencmis.commons.impl.CollectionsHelper.isNullOrEmpty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.ObjectFactory;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.OperationContext;
import org.apache.chemistry.opencmis.client.api.Policy;
import org.apache.chemistry.opencmis.client.api.Property;
import org.apache.chemistry.opencmis.client.api.Relationship;
import org.apache.chemistry.opencmis.client.api.Rendition;
import org.apache.chemistry.opencmis.client.api.SecondaryType;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.Ace;
import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.CmisExtensionElement;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.ExtensionLevel;
import org.apache.chemistry.opencmis.commons.enums.Updatability;
import org.apache.chemistry.opencmis.commons.spi.CmisBinding;
import org.apache.chemistry.opencmis.commons.spi.Holder;

/**
 * Base class for all persistent session object impl classes.
 */
public abstract class AbstractCmisObject implements CmisObject, Serializable {

    private static final long serialVersionUID = 1L;

    private SessionImpl session;
    private ObjectType objectType;
    private List<SecondaryType> secondaryTypes;
    private Map<String, Property<?>> properties;
    private AllowableActions allowableActions;
    private List<Rendition> renditions;
    private Acl acl;
    private List<Policy> policies;
    private List<Relationship> relationships;
    private Map<ExtensionLevel, List<CmisExtensionElement>> extensions;
    private OperationContext creationContext;
    private long refreshTimestamp;

    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * Initializes the object.
     */
    protected void initialize(SessionImpl session, ObjectType objectType, ObjectData objectData,
            OperationContext context) {
        if (session == null) {
            throw new IllegalArgumentException("Session must be set!");
        }

        if (objectType == null) {
            throw new IllegalArgumentException("Object type must be set!");
        }

        if (objectType.getPropertyDefinitions() == null || objectType.getPropertyDefinitions().size() < 9) {
            // there must be at least the 9 standard properties that all objects
            // have
            throw new IllegalArgumentException("Object type must have property definitions!");
        }

        if (objectData == null) {
            throw new IllegalArgumentException("Object data must be set!");
        }

        if (objectData.getProperties() == null) {
            throw new IllegalArgumentException("Properties must be set!");
        }

        if (objectData.getId() == null) {
            throw new IllegalArgumentException("Object ID must be set!");
        }

        this.session = session;
        this.objectType = objectType;
        this.secondaryTypes = null;
        this.extensions = new EnumMap<ExtensionLevel, List<CmisExtensionElement>>(ExtensionLevel.class);
        this.creationContext = new OperationContextImpl(context);
        this.refreshTimestamp = System.currentTimeMillis();

        ObjectFactory of = getObjectFactory();

        // get secondary types
        if (objectData.getProperties().getProperties() != null
                && objectData.getProperties().getProperties().containsKey(PropertyIds.SECONDARY_OBJECT_TYPE_IDS)) {
            @SuppressWarnings("unchecked")
            List<String> stids = (List<String>) objectData.getProperties().getProperties()
                    .get(PropertyIds.SECONDARY_OBJECT_TYPE_IDS).getValues();
            if (isNotEmpty(stids)) {
                secondaryTypes = new ArrayList<SecondaryType>();
                for (String stid : stids) {
                    if (stid != null) {
                        ObjectType type = session.getTypeDefinition(stid);
                        if (type instanceof SecondaryType) {
                            secondaryTypes.add((SecondaryType) type);
                        }
                    }
                }
            } else {
                secondaryTypes = null;
            }
        }

        // handle properties
        this.properties = of.convertProperties(objectType, secondaryTypes, objectData.getProperties());
        extensions.put(ExtensionLevel.PROPERTIES, objectData.getProperties().getExtensions());

        // handle allowable actions
        if (objectData.getAllowableActions() != null) {
            allowableActions = objectData.getAllowableActions();
            extensions.put(ExtensionLevel.ALLOWABLE_ACTIONS, objectData.getAllowableActions().getExtensions());
        } else {
            allowableActions = null;
        }

        // handle renditions
        if (objectData.getRenditions() != null && !objectData.getRenditions().isEmpty()) {
            renditions = new ArrayList<Rendition>();
            for (RenditionData rd : objectData.getRenditions()) {
                renditions.add(of.convertRendition(getId(), rd));
            }
        } else {
            renditions = null;
        }

        // handle ACL
        if (objectData.getAcl() != null) {
            acl = objectData.getAcl();
            extensions.put(ExtensionLevel.ACL, objectData.getAcl().getExtensions());

            if (objectData.isExactAcl() != null) {
                final Acl objectAcl = objectData.getAcl();
                final Boolean isExact = objectData.isExactAcl();
                acl = new Acl() {

                    @Override
                    public void setExtensions(List<CmisExtensionElement> extensions) {
                        objectAcl.setExtensions(extensions);
                    }

                    @Override
                    public List<CmisExtensionElement> getExtensions() {
                        return objectAcl.getExtensions();
                    }

                    @Override
                    public Boolean isExact() {
                        return isExact;
                    }

                    @Override
                    public List<Ace> getAces() {
                        return objectAcl.getAces();
                    }
                };
            }
        } else {
            acl = null;
        }

        // handle policies
        if (objectData.getPolicyIds() != null && objectData.getPolicyIds().getPolicyIds() != null) {
            if (objectData.getPolicyIds().getPolicyIds().isEmpty()) {
                policies = null;
            } else {
                policies = new ArrayList<Policy>();
                for (String pid : objectData.getPolicyIds().getPolicyIds()) {
                    CmisObject policy = session.getObject(pid);
                    if (policy instanceof Policy) {
                        policies.add((Policy) policy);
                    }
                }
            }
            extensions.put(ExtensionLevel.POLICIES, objectData.getPolicyIds().getExtensions());
        } else {
            policies = null;
        }

        // handle relationships
        if (objectData.getRelationships() != null && !objectData.getRelationships().isEmpty()) {
            relationships = new ArrayList<Relationship>();
            for (ObjectData rod : objectData.getRelationships()) {
                CmisObject relationship = of.convertObject(rod, this.creationContext);
                if (relationship instanceof Relationship) {
                    relationships.add((Relationship) relationship);
                }
            }
        } else {
            relationships = null;
        }

        extensions.put(ExtensionLevel.OBJECT, objectData.getExtensions());
    }

    /**
     * Acquires a write lock.
     */
    protected void writeLock() {
        lock.writeLock().lock();
    }

    /**
     * Releases a write lock.
     */
    protected void writeUnlock() {
        lock.writeLock().unlock();
    }

    /**
     * Acquires a read lock.
     */
    protected void readLock() {
        lock.readLock().lock();
    }

    /**
     * Releases a read lock.
     */
    protected void readUnlock() {
        lock.readLock().unlock();
    }

    /**
     * Returns the session object.
     */
    protected SessionImpl getSession() {
        return session;
    }

    /**
     * Returns the repository id.
     */
    protected String getRepositoryId() {
        return getSession().getRepositoryId();
    }

    /**
     * Returns the object type.
     */
    protected ObjectType getObjectType() {
        readLock();
        try {
            return objectType;
        } finally {
            readUnlock();
        }
    }

    /**
     * Returns the binding object.
     */
    protected CmisBinding getBinding() {
        return getSession().getBinding();
    }

    /**
     * Returns the object factory.
     */
    protected ObjectFactory getObjectFactory() {
        return getSession().getObjectFactory();
    }

    /**
     * Returns the id of this object or throws an exception if the id is
     * unknown.
     */
    protected String getObjectId() {
        String objectId = getId();
        if (objectId == null) {
            throw new IllegalStateException("Object Id is unknown!");
        }

        return objectId;
    }

    /**
     * Returns the {@link OperationContext} that was used to create this object.
     */
    protected OperationContext getCreationContext() {
        return creationContext;
    }

    /**
     * Returns the query name of a property.
     */
    protected String getPropertyQueryName(String propertyId) {
        readLock();
        try {
            PropertyDefinition<?> propDef = objectType.getPropertyDefinitions().get(propertyId);
            if (propDef == null) {
                return null;
            }

            return propDef.getQueryName();
        } finally {
            readUnlock();
        }
    }

    // --- delete ---

    public void delete() {
        delete(true);
    }

    public void delete(boolean allVersions) {
        readLock();
        try {
            getSession().delete(this, allVersions);
        } finally {
            readUnlock();
        }
    }

    // --- update properties ---

    public CmisObject updateProperties(Map<String, ?> properties) {
        ObjectId objectId = updateProperties(properties, true);
        if (objectId == null) {
            return null;
        }

        if (!getObjectId().equals(objectId.getId())) {
            return getSession().getObject(objectId, getCreationContext());
        }

        return this;
    }

    public ObjectId updateProperties(Map<String, ?> properties, boolean refresh) {
        if (isNullOrEmpty(properties)) {
            throw new IllegalArgumentException("Properties must not be empty!");
        }

        readLock();
        String newObjectId = null;
        try {
            String objectId = getObjectId();
            Holder<String> objectIdHolder = new Holder<String>(objectId);

            String changeToken = getChangeToken();
            Holder<String> changeTokenHolder = new Holder<String>(changeToken);

            Set<Updatability> updatebility = EnumSet.noneOf(Updatability.class);
            updatebility.add(Updatability.READWRITE);

            // check if checked out
            Boolean isCheckedOut = getPropertyValue(PropertyIds.IS_VERSION_SERIES_CHECKED_OUT);
            if (Boolean.TRUE.equals(isCheckedOut)) {
                updatebility.add(Updatability.WHENCHECKEDOUT);
            }

            // it's time to update
            getBinding().getObjectService().updateProperties(
                    getRepositoryId(),
                    objectIdHolder,
                    changeTokenHolder,
                    getObjectFactory()
                            .convertProperties(properties, this.objectType, this.secondaryTypes, updatebility), null);

            newObjectId = objectIdHolder.getValue();

            // remove the object from the cache, it has been changed
            getSession().removeObjectFromCache(objectId);
        } finally {
            readUnlock();
        }

        if (refresh) {
            refresh();
        }

        if (newObjectId == null) {
            return null;
        }

        return getSession().createObjectId(newObjectId);
    }

    public CmisObject rename(String newName) {
        if (newName == null || newName.length() == 0) {
            throw new IllegalArgumentException("New name must not be empty!");
        }

        Map<String, Object> prop = new HashMap<String, Object>();
        prop.put(PropertyIds.NAME, newName);

        return updateProperties(prop);
    }

    public ObjectId rename(String newName, boolean refresh) {
        if (newName == null || newName.length() == 0) {
            throw new IllegalArgumentException("New name must not be empty!");
        }

        Map<String, Object> prop = new HashMap<String, Object>();
        prop.put(PropertyIds.NAME, newName);

        return updateProperties(prop, refresh);
    }

    // --- properties ---

    public ObjectType getBaseType() {
        BaseTypeId baseTypeId = getBaseTypeId();
        if (baseTypeId == null) {
            return null;
        }

        return getSession().getTypeDefinition(baseTypeId.value());
    }

    public BaseTypeId getBaseTypeId() {
        String baseType = getPropertyValue(PropertyIds.BASE_TYPE_ID);
        if (baseType == null) {
            return null;
        }

        return BaseTypeId.fromValue(baseType);
    }

    public String getChangeToken() {
        return getPropertyValue(PropertyIds.CHANGE_TOKEN);
    }

    public String getCreatedBy() {
        return getPropertyValue(PropertyIds.CREATED_BY);
    }

    public GregorianCalendar getCreationDate() {
        return getPropertyValue(PropertyIds.CREATION_DATE);
    }

    public String getId() {
        return getPropertyValue(PropertyIds.OBJECT_ID);
    }

    public GregorianCalendar getLastModificationDate() {
        return getPropertyValue(PropertyIds.LAST_MODIFICATION_DATE);
    }

    public String getLastModifiedBy() {
        return getPropertyValue(PropertyIds.LAST_MODIFIED_BY);
    }

    public String getName() {
        return getPropertyValue(PropertyIds.NAME);
    }

    public String getDescription() {
        return getPropertyValue(PropertyIds.DESCRIPTION);
    }

    public List<Property<?>> getProperties() {
        readLock();
        try {
            return Collections.unmodifiableList(new ArrayList<Property<?>>(properties.values()));
        } finally {
            readUnlock();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Property<T> getProperty(String id) {
        readLock();
        try {
            return (Property<T>) properties.get(id);
        } finally {
            readUnlock();
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T getPropertyValue(String id) {
        Property<T> property = getProperty(id);
        if (property == null) {
            return null;
        }
        // explicit cast needed by the Sun compiler
        return (T) property.getValue();
    }

    public ObjectType getType() {
        readLock();
        try {
            return this.objectType;
        } finally {
            readUnlock();
        }
    }

    public List<SecondaryType> getSecondaryTypes() {
        readLock();
        try {
            return secondaryTypes;
        } finally {
            readUnlock();
        }
    }

    public List<ObjectType> findObjectType(String id) {
        List<ObjectType> result = null;

        readLock();
        try {
            if (objectType.getPropertyDefinitions().containsKey(id)) {
                result = new ArrayList<ObjectType>();
                result.add(objectType);
            }

            if (secondaryTypes != null) {
                for (SecondaryType secondaryType : secondaryTypes) {
                    if (secondaryType.getPropertyDefinitions() != null
                            && secondaryType.getPropertyDefinitions().containsKey(id)) {
                        if (result == null) {
                            result = new ArrayList<ObjectType>();
                        }
                        result.add(secondaryType);
                    }
                }
            }
        } finally {
            readUnlock();
        }

        return result;
    }

    // --- allowable actions ---

    public AllowableActions getAllowableActions() {
        readLock();
        try {
            return this.allowableActions;
        } finally {
            readUnlock();
        }
    }

    public boolean hasAllowableAction(Action action) {
        if (action == null) {
            throw new IllegalArgumentException("Action must be set!");
        }

        AllowableActions currentAllowableActions = getAllowableActions();
        if (currentAllowableActions == null || currentAllowableActions.getAllowableActions() == null) {
            throw new IllegalStateException("Allowable Actions are not available!");
        }

        return currentAllowableActions.getAllowableActions().contains(action);
    }

    // --- renditions ---

    public List<Rendition> getRenditions() {
        readLock();
        try {
            return this.renditions;
        } finally {
            readUnlock();
        }
    }

    // --- ACL ---

    public Acl getAcl(boolean onlyBasicPermissions) {
        String objectId = getObjectId();
        return getBinding().getAclService().getAcl(getRepositoryId(), objectId, onlyBasicPermissions, null);
    }

    public Acl applyAcl(List<Ace> addAces, List<Ace> removeAces, AclPropagation aclPropagation) {
        Acl result = getSession().applyAcl(this, addAces, removeAces, aclPropagation);

        refresh();

        return result;
    }

    public Acl addAcl(List<Ace> addAces, AclPropagation aclPropagation) {
        return applyAcl(addAces, null, aclPropagation);
    }

    public Acl removeAcl(List<Ace> removeAces, AclPropagation aclPropagation) {
        return applyAcl(null, removeAces, aclPropagation);
    }

    public Acl setAcl(List<Ace> aces) {
        Acl result = getSession().setAcl(this, aces);

        refresh();

        return result;
    }

    public Acl getAcl() {
        readLock();
        try {
            return acl;
        } finally {
            readUnlock();
        }
    }

    public Set<String> getPermissionsForPrincipal(String principalId) {
        if (principalId == null) {
            throw new IllegalArgumentException("Principal must be set!");
        }

        Acl currentAcl = getAcl();

        if (currentAcl == null) {
            throw new IllegalStateException("ACLs are not available!");
        }

        if (isNullOrEmpty(acl.getAces())) {
            return Collections.emptySet();
        }

        HashSet<String> result = new HashSet<String>();

        for (Ace ace : acl.getAces()) {
            if (principalId.equals(ace.getPrincipalId()) && ace.getPermissions() != null) {
                result.addAll(ace.getPermissions());
            }
        }

        return result;
    }

    public Set<String> getPermissonsForPrincipal(String principalId) {
        return getPermissionsForPrincipal(principalId);
    }

    // --- policies ---

    public void applyPolicy(ObjectId... policyIds) {
        readLock();
        try {
            getSession().applyPolicy(this, policyIds);
        } finally {
            readUnlock();
        }

        refresh();
    }

    public void applyPolicy(ObjectId policyId, boolean refresh) {
        readLock();
        try {
            getSession().applyPolicy(this, policyId);
        } finally {
            readUnlock();
        }

        if (refresh) {
            refresh();
        }
    }

    public void removePolicy(ObjectId... policyIds) {
        readLock();
        try {
            getSession().removePolicy(this, policyIds);
        } finally {
            readUnlock();
        }

        refresh();
    }

    public void removePolicy(ObjectId policyId, boolean refresh) {
        readLock();
        try {
            getSession().removePolicy(this, policyId);
        } finally {
            readUnlock();
        }

        if (refresh) {
            refresh();
        }
    }

    public List<Policy> getPolicies() {
        readLock();
        try {
            return policies;
        } finally {
            readUnlock();
        }
    }

    // --- relationships ---

    public List<Relationship> getRelationships() {
        readLock();
        try {
            return this.relationships;
        } finally {
            readUnlock();
        }
    }

    // --- extensions ---

    public List<CmisExtensionElement> getExtensions(ExtensionLevel level) {
        List<CmisExtensionElement> ext = extensions.get(level);
        if (ext == null) {
            return null;
        }

        return Collections.unmodifiableList(ext);
    }

    // --- adapters ---

    public <T> T getAdapter(Class<T> adapterInterface) {
        return null;
    }

    // --- other ---

    public long getRefreshTimestamp() {
        readLock();
        try {
            return this.refreshTimestamp;
        } finally {
            readUnlock();
        }
    }

    public void refresh() {
        writeLock();
        try {
            String objectId = getObjectId();

            OperationContext oc = getCreationContext();

            // get the latest data from the repository
            ObjectData objectData = getSession()
                    .getBinding()
                    .getObjectService()
                    .getObject(getRepositoryId(), objectId, oc.getFilterString(), oc.isIncludeAllowableActions(),
                            oc.getIncludeRelationships(), oc.getRenditionFilterString(), oc.isIncludePolicies(),
                            oc.isIncludeAcls(), null);

            // reset this object
            initialize(session, session.getTypeDefinition(objectType.getId()), objectData, creationContext);
        } finally {
            writeUnlock();
        }
    }

    public void refreshIfOld(long durationInMillis) {
        writeLock();
        try {
            if (this.refreshTimestamp < System.currentTimeMillis() - durationInMillis) {
                refresh();
            }
        } finally {
            writeUnlock();
        }
    }

    @Override
    public String toString() {
        readLock();
        try {
            if (objectType == null) {
                return "<unknown>";
            }

            return objectType.getBaseTypeId() + " (" + objectType.getId() + "): " + getId();
        } finally {
            readUnlock();
        }
    }
}
