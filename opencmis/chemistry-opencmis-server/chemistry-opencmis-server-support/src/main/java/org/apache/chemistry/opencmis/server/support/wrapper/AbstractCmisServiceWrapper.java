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
package org.apache.chemistry.opencmis.server.support.wrapper;

import java.math.BigInteger;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.Acl;
import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.data.BulkUpdateObjectIdAndChangeToken;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.FailedToDeleteData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderContainer;
import org.apache.chemistry.opencmis.commons.data.ObjectInFolderList;
import org.apache.chemistry.opencmis.commons.data.ObjectList;
import org.apache.chemistry.opencmis.commons.data.ObjectParentData;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.data.RenditionData;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionContainer;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.AclPropagation;
import org.apache.chemistry.opencmis.commons.enums.IncludeRelationships;
import org.apache.chemistry.opencmis.commons.enums.RelationshipDirection;
import org.apache.chemistry.opencmis.commons.enums.UnfileObject;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.ObjectInfo;
import org.apache.chemistry.opencmis.commons.server.ProgressControlCmisService;
import org.apache.chemistry.opencmis.commons.spi.Holder;

/**
 * An abstract CMIS service wrapper.
 * <p>
 * All service wrappers managed by {@link CmisServiceWrapperManager} must be
 * derived from this class and must provide a constructor that takes a
 * {@link CmisService} object as the sole parameter.
 */
public abstract class AbstractCmisServiceWrapper implements CallContextAwareCmisService, ProgressControlCmisService {

    private CmisService service;
    private CallContext context;

    public AbstractCmisServiceWrapper(CmisService service) {
        if (service == null) {
            throw new IllegalArgumentException("Service must be set!");
        }

        this.service = service;
    }

    /**
     * Initializes the wrapper with a set of parameters.
     */
    public void initialize(Object[] params) {
    }

    /**
     * Returns the wrapped service or the next service wrapper.
     * 
     * @return the wrapped service
     */
    public CmisService getWrappedService() {
        return service;
    }

    /**
     * Sets the call context and propagates it down to the next service wrapper
     * or service if it implements the {@link CallContextAwareCmisService}
     * interface.
     */
    public void setCallContext(CallContext callContext) {
        this.context = callContext;

        if (service instanceof CallContextAwareCmisService) {
            ((CallContextAwareCmisService) service).setCallContext(callContext);
        }
    }

    /**
     * Gets the current call context.
     */
    public CallContext getCallContext() {
        return context;
    }

    // --- processing ---

    public ProgressControlCmisService.Progress beforeServiceCall() {
        if (service instanceof ProgressControlCmisService) {
            return ((ProgressControlCmisService) service).beforeServiceCall();
        }

        return ProgressControlCmisService.Progress.CONTINUE;
    }

    public ProgressControlCmisService.Progress afterServiceCall() {
        if (service instanceof ProgressControlCmisService) {
            return ((ProgressControlCmisService) service).afterServiceCall();
        }

        return ProgressControlCmisService.Progress.CONTINUE;
    }

    // --- service methods ---

    public List<RepositoryInfo> getRepositoryInfos(ExtensionsData extension) {
        return service.getRepositoryInfos(extension);
    }

    public RepositoryInfo getRepositoryInfo(String repositoryId, ExtensionsData extension) {
        return service.getRepositoryInfo(repositoryId, extension);
    }

    public TypeDefinitionList getTypeChildren(String repositoryId, String typeId, Boolean includePropertyDefinitions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return service
                .getTypeChildren(repositoryId, typeId, includePropertyDefinitions, maxItems, skipCount, extension);
    }

    public List<TypeDefinitionContainer> getTypeDescendants(String repositoryId, String typeId, BigInteger depth,
            Boolean includePropertyDefinitions, ExtensionsData extension) {
        return service.getTypeDescendants(repositoryId, typeId, depth, includePropertyDefinitions, extension);
    }

    public TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension) {
        return service.getTypeDefinition(repositoryId, typeId, extension);
    }

    public TypeDefinition createType(String repositoryId, TypeDefinition type, ExtensionsData extension) {
        return service.createType(repositoryId, type, extension);
    }

    public TypeDefinition updateType(String repositoryId, TypeDefinition type, ExtensionsData extension) {
        return service.updateType(repositoryId, type, extension);
    }

    public void deleteType(String repositoryId, String typeId, ExtensionsData extension) {
        service.deleteType(repositoryId, typeId, extension);
    }

    public ObjectInFolderList getChildren(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includePathSegment, BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return service.getChildren(repositoryId, folderId, filter, orderBy, includeAllowableActions,
                includeRelationships, renditionFilter, includePathSegment, maxItems, skipCount, extension);
    }

    public List<ObjectInFolderContainer> getDescendants(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        return service.getDescendants(repositoryId, folderId, depth, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includePathSegment, extension);
    }

    public List<ObjectInFolderContainer> getFolderTree(String repositoryId, String folderId, BigInteger depth,
            String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePathSegment, ExtensionsData extension) {
        return service.getFolderTree(repositoryId, folderId, depth, filter, includeAllowableActions,
                includeRelationships, renditionFilter, includePathSegment, extension);
    }

    public List<ObjectParentData> getObjectParents(String repositoryId, String objectId, String filter,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            Boolean includeRelativePathSegment, ExtensionsData extension) {
        return service.getObjectParents(repositoryId, objectId, filter, includeAllowableActions, includeRelationships,
                renditionFilter, includeRelativePathSegment, extension);
    }

    public ObjectData getFolderParent(String repositoryId, String folderId, String filter, ExtensionsData extension) {
        return service.getFolderParent(repositoryId, folderId, filter, extension);
    }

    public ObjectList getCheckedOutDocs(String repositoryId, String folderId, String filter, String orderBy,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return service.getCheckedOutDocs(repositoryId, folderId, filter, orderBy, includeAllowableActions,
                includeRelationships, renditionFilter, maxItems, skipCount, extension);
    }

    public String createDocument(String repositoryId, Properties properties, String folderId,
            ContentStream contentStream, VersioningState versioningState, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        return service.createDocument(repositoryId, properties, folderId, contentStream, versioningState, policies,
                addAces, removeAces, extension);
    }

    public String createDocumentFromSource(String repositoryId, String sourceId, Properties properties,
            String folderId, VersioningState versioningState, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        return service.createDocumentFromSource(repositoryId, sourceId, properties, folderId, versioningState,
                policies, addAces, removeAces, extension);
    }

    public String createFolder(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        return service.createFolder(repositoryId, properties, folderId, policies, addAces, removeAces, extension);
    }

    public String createRelationship(String repositoryId, Properties properties, List<String> policies, Acl addAces,
            Acl removeAces, ExtensionsData extension) {
        return service.createRelationship(repositoryId, properties, policies, addAces, removeAces, extension);
    }

    public String createPolicy(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        return service.createPolicy(repositoryId, properties, folderId, policies, addAces, removeAces, extension);
    }

    public String createItem(String repositoryId, Properties properties, String folderId, List<String> policies,
            Acl addAces, Acl removeAces, ExtensionsData extension) {
        return service.createItem(repositoryId, properties, folderId, policies, addAces, removeAces, extension);
    }

    public AllowableActions getAllowableActions(String repositoryId, String objectId, ExtensionsData extension) {
        return service.getAllowableActions(repositoryId, objectId, extension);
    }

    public ObjectData getObject(String repositoryId, String objectId, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        return service.getObject(repositoryId, objectId, filter, includeAllowableActions, includeRelationships,
                renditionFilter, includePolicyIds, includeAcl, extension);
    }

    public Properties getProperties(String repositoryId, String objectId, String filter, ExtensionsData extension) {
        return service.getProperties(repositoryId, objectId, filter, extension);
    }

    public List<RenditionData> getRenditions(String repositoryId, String objectId, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return service.getRenditions(repositoryId, objectId, renditionFilter, maxItems, skipCount, extension);
    }

    public ObjectData getObjectByPath(String repositoryId, String path, String filter, Boolean includeAllowableActions,
            IncludeRelationships includeRelationships, String renditionFilter, Boolean includePolicyIds,
            Boolean includeAcl, ExtensionsData extension) {
        return service.getObjectByPath(repositoryId, path, filter, includeAllowableActions, includeRelationships,
                renditionFilter, includePolicyIds, includeAcl, extension);
    }

    public ContentStream getContentStream(String repositoryId, String objectId, String streamId, BigInteger offset,
            BigInteger length, ExtensionsData extension) {
        return service.getContentStream(repositoryId, objectId, streamId, offset, length, extension);
    }

    public void updateProperties(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            Properties properties, ExtensionsData extension) {
        service.updateProperties(repositoryId, objectId, changeToken, properties, extension);
    }

    public List<BulkUpdateObjectIdAndChangeToken> bulkUpdateProperties(String repositoryId,
            List<BulkUpdateObjectIdAndChangeToken> objectIdsAndChangeTokens, Properties properties,
            List<String> addSecondaryTypeIds, List<String> removeSecondaryTypeIds, ExtensionsData extension) {
        return service.bulkUpdateProperties(repositoryId, objectIdsAndChangeTokens, properties, addSecondaryTypeIds,
                removeSecondaryTypeIds, extension);
    }

    public void moveObject(String repositoryId, Holder<String> objectId, String targetFolderId, String sourceFolderId,
            ExtensionsData extension) {
        service.moveObject(repositoryId, objectId, targetFolderId, sourceFolderId, extension);
    }

    public void deleteObject(String repositoryId, String objectId, Boolean allVersions, ExtensionsData extension) {
        service.deleteObject(repositoryId, objectId, allVersions, extension);
    }

    public FailedToDeleteData deleteTree(String repositoryId, String folderId, Boolean allVersions,
            UnfileObject unfileObjects, Boolean continueOnFailure, ExtensionsData extension) {
        return service.deleteTree(repositoryId, folderId, allVersions, unfileObjects, continueOnFailure, extension);
    }

    public void setContentStream(String repositoryId, Holder<String> objectId, Boolean overwriteFlag,
            Holder<String> changeToken, ContentStream contentStream, ExtensionsData extension) {
        service.setContentStream(repositoryId, objectId, overwriteFlag, changeToken, contentStream, extension);
    }

    public void deleteContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            ExtensionsData extension) {
        service.deleteContentStream(repositoryId, objectId, changeToken, extension);
    }

    public void appendContentStream(String repositoryId, Holder<String> objectId, Holder<String> changeToken,
            ContentStream contentStream, boolean isLastChunk, ExtensionsData extension) {
        service.appendContentStream(repositoryId, objectId, changeToken, contentStream, isLastChunk, extension);
    }

    public void checkOut(String repositoryId, Holder<String> objectId, ExtensionsData extension,
            Holder<Boolean> contentCopied) {
        service.checkOut(repositoryId, objectId, extension, contentCopied);
    }

    public void cancelCheckOut(String repositoryId, String objectId, ExtensionsData extension) {
        service.cancelCheckOut(repositoryId, objectId, extension);
    }

    public void checkIn(String repositoryId, Holder<String> objectId, Boolean major, Properties properties,
            ContentStream contentStream, String checkinComment, List<String> policies, Acl addAces, Acl removeAces,
            ExtensionsData extension) {
        service.checkIn(repositoryId, objectId, major, properties, contentStream, checkinComment, policies, addAces,
                removeAces, extension);
    }

    public ObjectData getObjectOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, Boolean includeAllowableActions, IncludeRelationships includeRelationships,
            String renditionFilter, Boolean includePolicyIds, Boolean includeAcl, ExtensionsData extension) {
        return service
                .getObjectOfLatestVersion(repositoryId, objectId, versionSeriesId, major, filter,
                        includeAllowableActions, includeRelationships, renditionFilter, includePolicyIds, includeAcl,
                        extension);
    }

    public Properties getPropertiesOfLatestVersion(String repositoryId, String objectId, String versionSeriesId,
            Boolean major, String filter, ExtensionsData extension) {
        return service.getPropertiesOfLatestVersion(repositoryId, objectId, versionSeriesId, major, filter, extension);
    }

    public List<ObjectData> getAllVersions(String repositoryId, String objectId, String versionSeriesId, String filter,
            Boolean includeAllowableActions, ExtensionsData extension) {
        return service.getAllVersions(repositoryId, objectId, versionSeriesId, filter, includeAllowableActions,
                extension);
    }

    public ObjectList query(String repositoryId, String statement, Boolean searchAllVersions,
            Boolean includeAllowableActions, IncludeRelationships includeRelationships, String renditionFilter,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return service.query(repositoryId, statement, searchAllVersions, includeAllowableActions, includeRelationships,
                renditionFilter, maxItems, skipCount, extension);
    }

    public ObjectList getContentChanges(String repositoryId, Holder<String> changeLogToken, Boolean includeProperties,
            String filter, Boolean includePolicyIds, Boolean includeAcl, BigInteger maxItems, ExtensionsData extension) {
        return service.getContentChanges(repositoryId, changeLogToken, includeProperties, filter, includePolicyIds,
                includeAcl, maxItems, extension);
    }

    public void addObjectToFolder(String repositoryId, String objectId, String folderId, Boolean allVersions,
            ExtensionsData extension) {
        service.addObjectToFolder(repositoryId, objectId, folderId, allVersions, extension);
    }

    public void removeObjectFromFolder(String repositoryId, String objectId, String folderId, ExtensionsData extension) {
        service.removeObjectFromFolder(repositoryId, objectId, folderId, extension);
    }

    public ObjectList getObjectRelationships(String repositoryId, String objectId, Boolean includeSubRelationshipTypes,
            RelationshipDirection relationshipDirection, String typeId, String filter, Boolean includeAllowableActions,
            BigInteger maxItems, BigInteger skipCount, ExtensionsData extension) {
        return service.getObjectRelationships(repositoryId, objectId, includeSubRelationshipTypes,
                relationshipDirection, typeId, filter, includeAllowableActions, maxItems, skipCount, extension);
    }

    public Acl getAcl(String repositoryId, String objectId, Boolean onlyBasicPermissions, ExtensionsData extension) {
        return service.getAcl(repositoryId, objectId, onlyBasicPermissions, extension);
    }

    public Acl applyAcl(String repositoryId, String objectId, Acl addAces, Acl removeAces,
            AclPropagation aclPropagation, ExtensionsData extension) {
        return service.applyAcl(repositoryId, objectId, addAces, removeAces, aclPropagation, extension);
    }

    public void applyPolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        service.applyPolicy(repositoryId, policyId, objectId, extension);
    }

    public void removePolicy(String repositoryId, String policyId, String objectId, ExtensionsData extension) {
        service.removePolicy(repositoryId, policyId, objectId, extension);
    }

    public List<ObjectData> getAppliedPolicies(String repositoryId, String objectId, String filter,
            ExtensionsData extension) {
        return service.getAppliedPolicies(repositoryId, objectId, filter, extension);
    }

    public String create(String repositoryId, Properties properties, String folderId, ContentStream contentStream,
            VersioningState versioningState, List<String> policies, ExtensionsData extension) {
        return service.create(repositoryId, properties, folderId, contentStream, versioningState, policies, extension);
    }

    public void deleteObjectOrCancelCheckOut(String repositoryId, String objectId, Boolean allVersions,
            ExtensionsData extension) {
        service.deleteObjectOrCancelCheckOut(repositoryId, objectId, allVersions, extension);
    }

    public Acl applyAcl(String repositoryId, String objectId, Acl aces, AclPropagation aclPropagation) {
        return service.applyAcl(repositoryId, objectId, aces, aclPropagation);
    }

    public ObjectInfo getObjectInfo(String repositoryId, String objectId) {
        return service.getObjectInfo(repositoryId, objectId);
    }

    public void close() {
        service.close();
        context = null;
    }
}
