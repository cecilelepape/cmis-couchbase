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
package org.apache.chemistry.opencmis.tck.tests.types;

import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.FAILURE;
import static org.apache.chemistry.opencmis.tck.CmisTestResultStatus.WARNING;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinitionList;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.tck.CmisTestResult;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

public class BaseTypesTest extends AbstractSessionTest {
    @Override
    public void init(Map<String, String> parameters) {
        super.init(parameters);
        setName("Types Test");
        setDescription("Checks all types exposed by the repository for specification compliance.");
    }

    @Override
    public void run(Session session) {
        CmisTestResult failure;

        // check base types
        Set<String> cmisTypes = new HashSet<String>();
        cmisTypes.add(BaseTypeId.CMIS_DOCUMENT.value());
        cmisTypes.add(BaseTypeId.CMIS_FOLDER.value());
        cmisTypes.add(BaseTypeId.CMIS_RELATIONSHIP.value());
        cmisTypes.add(BaseTypeId.CMIS_POLICY.value());

        if (session.getRepositoryInfo().getCmisVersion() != CmisVersion.CMIS_1_0) {
            cmisTypes.add(BaseTypeId.CMIS_ITEM.value());
            cmisTypes.add(BaseTypeId.CMIS_SECONDARY.value());
        }

        for (TypeDefinition typeDef : session.getTypeChildren(null, false)) {
            String typeId = typeDef.getId();

            if (typeId == null || !cmisTypes.contains(typeId)) {
                addResult(createResult(FAILURE, "Base type has an invalid ID: " + typeId));
            }

            if (typeDef.getPropertyDefinitions() != null && !typeDef.getPropertyDefinitions().isEmpty()) {
                addResult(createResult(WARNING, "Property type definitions were not requested but delivered. Type ID: "
                        + typeId));
            }
        }

        // document
        try {
            TypeDefinition documentType = session.getTypeDefinition(BaseTypeId.CMIS_DOCUMENT.value());
            addResult(checkTypeDefinition(session, documentType,
                    "Document type spec compliance: " + documentType.getId()));

            failure = createResult(FAILURE, "Document type has the wrong base type: " + documentType.getBaseTypeId());
            addResult(assertEquals(BaseTypeId.CMIS_DOCUMENT, documentType.getBaseTypeId(), null, failure));
        } catch (CmisObjectNotFoundException e) {
            addResult(createResult(FAILURE, "Document type not available!", e, false));
        }

        // folder
        try {
            TypeDefinition folderType = session.getTypeDefinition(BaseTypeId.CMIS_FOLDER.value());

            addResult(checkTypeDefinition(session, folderType, "Folder type spec compliance: " + folderType.getId()));

            failure = createResult(FAILURE, "Folder type has the wrong base type: " + folderType.getBaseTypeId());
            addResult(assertEquals(BaseTypeId.CMIS_FOLDER, folderType.getBaseTypeId(), null, failure));
        } catch (CmisObjectNotFoundException e) {
            addResult(createResult(FAILURE, "Folder type not available!", e, false));
        }

        // relationship
        try {
            TypeDefinition relationshipType = session.getTypeDefinition(BaseTypeId.CMIS_RELATIONSHIP.value());
            addResult(checkTypeDefinition(session, relationshipType, "Relationship type spec compliance: "
                    + relationshipType.getId()));

            failure = createResult(FAILURE,
                    "Relationship type has the wrong base type: " + relationshipType.getBaseTypeId());
            addResult(assertEquals(BaseTypeId.CMIS_RELATIONSHIP, relationshipType.getBaseTypeId(), null, failure));
        } catch (CmisObjectNotFoundException e) {
            addResult(createResult(WARNING, "Relationship type not available!", e, false));
        }

        // policy
        try {
            TypeDefinition policyType = session.getTypeDefinition(BaseTypeId.CMIS_POLICY.value());
            addResult(checkTypeDefinition(session, policyType, "Policy type spec compliance: " + policyType.getId()));

            failure = createResult(FAILURE, "Policy type has the wrong base type: " + policyType.getBaseTypeId());
            addResult(assertEquals(BaseTypeId.CMIS_POLICY, policyType.getBaseTypeId(), null, failure));
        } catch (CmisInvalidArgumentException e) {
            addResult(createResult(WARNING, "Policy type not available!", e, false));
        } catch (CmisObjectNotFoundException e) {
            addResult(createResult(WARNING, "Policy type not available!", e, false));
        }

        // CMIS 1.1 types
        if (session.getRepositoryInfo().getCmisVersion() == CmisVersion.CMIS_1_1) {
            // item
            try {
                TypeDefinition itemType = session.getTypeDefinition(BaseTypeId.CMIS_ITEM.value());
                addResult(checkTypeDefinition(session, itemType, "Item type spec compliance: " + itemType.getId()));

                failure = createResult(FAILURE, "Item type has the wrong base type: " + itemType.getBaseTypeId());
                addResult(assertEquals(BaseTypeId.CMIS_ITEM, itemType.getBaseTypeId(), null, failure));
            } catch (CmisInvalidArgumentException e) {
                addResult(createResult(WARNING, "Item type not available!", e, false));
            } catch (CmisObjectNotFoundException e) {
                addResult(createResult(WARNING, "Item type not available!", e, false));
            }

            // secondary type
            try {
                TypeDefinition secondaryType = session.getTypeDefinition(BaseTypeId.CMIS_SECONDARY.value());
                addResult(checkTypeDefinition(session, secondaryType, "Secondary type spec compliance: "
                        + secondaryType.getId()));

                failure = createResult(FAILURE,
                        "Secondary type has the wrong base type: " + secondaryType.getBaseTypeId());
                addResult(assertEquals(BaseTypeId.CMIS_SECONDARY, secondaryType.getBaseTypeId(), null, failure));
            } catch (CmisInvalidArgumentException e) {
                addResult(createResult(WARNING, "Secondary type not available!", e, false));
            } catch (CmisObjectNotFoundException e) {
                addResult(createResult(WARNING, "Secondary type not available!", e, false));
            }
        } else {
            try {
                session.getTypeDefinition(BaseTypeId.CMIS_ITEM.value());
                addResult(createResult(FAILURE, "CMIS 1.0 repository returns cmis:item type definition!"));
            } catch (CmisBaseException e) {
                // expected
            }

            try {
                session.getTypeDefinition(BaseTypeId.CMIS_SECONDARY.value());
                addResult(createResult(FAILURE, "CMIS 1.0 repository returns cmis:secondary type definition!"));
            } catch (CmisBaseException e) {
                // expected
            }
        }

        // simple getTypeChildren paging test - skipping over all base types mut
        // return an empty list
        TypeDefinitionList typeDefinitionList = session
                .getBinding()
                .getRepositoryService()
                .getTypeChildren(session.getRepositoryInfo().getId(), null, false, BigInteger.valueOf(100),
                        BigInteger.valueOf(6), null);
        if (typeDefinitionList == null) {
            addResult(createResult(FAILURE, "getTypeChildren() returned nothing!"));
        } else {
            if (typeDefinitionList.getList() != null && !typeDefinitionList.getList().isEmpty()) {
                addResult(createResult(
                        FAILURE,
                        "A getTypeChildren() call on the base types must retrun an empty list if skipCount is >= 6! The repository returned a list of "
                                + typeDefinitionList.getList().size() + " elements."));
            }

            if (Boolean.TRUE.equals(typeDefinitionList.hasMoreItems())) {
                addResult(createResult(
                        FAILURE,
                        "A getTypeChildren() call on the base types must retrun an empty list if skipCount is >= 6! The repository returned hasMoreItems == true."));
            }
        }

        // test getTypeDescendants()
        int numOfTypes = runTypeChecks(session, session.getTypeDescendants(null, -1, true));

        addResult(createInfoResult("Checked " + numOfTypes + " type definitions."));
    }

    private int runTypeChecks(Session session, List<Tree<ObjectType>> types) {
        if (types == null) {
            return 0;
        }

        int numOfTypes = 0;
        CmisTestResult failure;

        for (Tree<ObjectType> tree : types) {
            failure = createResult(FAILURE, "Types tree contains null leaf!");
            addResult(assertNotNull(tree, null, failure));

            if (tree != null) {
                numOfTypes++;

                addResult(checkTypeDefinition(session, tree.getItem(), "Type spec compliance: "
                        + tree.getItem().getId()));

                // clear the cache to ensure that the type definition is
                // reloaded from the repository
                session.clear();

                try {
                    TypeDefinition reloadedType = session.getTypeDefinition(tree.getItem().getId());

                    addResult(checkTypeDefinition(session, reloadedType, "Type spec compliance: "
                            + (reloadedType == null ? "?" : reloadedType.getId())));

                    failure = createResult(FAILURE,
                            "Type fetched via getTypeDescendants() is does not macth type fetched via getTypeDefinition(): "
                                    + tree.getItem().getId());
                    addResult(assertEquals(tree.getItem(), reloadedType, null, failure));
                } catch (CmisObjectNotFoundException e) {
                    addResult(createResult(FAILURE,
                            "Type fetched via getTypeDescendants() is not available via getTypeDefinition(): "
                                    + tree.getItem().getId(), e, false));
                }

                // clear the cache again to ensure that the type definition
                // children are reloaded from the repository
                session.clear();

                try {
                    ItemIterable<ObjectType> reloadedTypeChildren = session.getTypeChildren(tree.getItem().getId(),
                            true);

                    // check type children
                    Map<String, ObjectType> typeChilden = new HashMap<String, ObjectType>();
                    for (ObjectType childType : reloadedTypeChildren) {
                        if (childType == null) {
                            addResult(createResult(FAILURE, "The list of types contains a null entry!"));
                            continue;
                        }

                        addResult(checkTypeDefinition(session, childType, "Type spec compliance: " + childType.getId()));

                        typeChilden.put(childType.getId(), childType);
                    }

                    // compare type children and type descendants
                    if (tree.getChildren() == null) {
                        failure = createResult(FAILURE,
                                "Type children fetched via getTypeDescendants() don't match type children fetched via getTypeChildren(): "
                                        + tree.getItem().getId());
                        addResult(assertEquals(0, typeChilden.size(), null, failure));
                    } else {
                        // collect the children
                        Map<String, ObjectType> typeDescendants = new HashMap<String, ObjectType>();
                        for (Tree<ObjectType> childType : tree.getChildren()) {
                            if ((childType != null) && (childType.getItem() != null)) {
                                typeDescendants.put(childType.getItem().getId(), childType.getItem());
                            }
                        }

                        failure = createResult(FAILURE,
                                "Type children fetched via getTypeDescendants() don't match type children fetched via getTypeChildren(): "
                                        + tree.getItem().getId());
                        addResult(assertEquals(typeDescendants.size(), typeChilden.size(), null, failure));

                        for (ObjectType compareType : typeDescendants.values()) {
                            failure = createResult(FAILURE,
                                    "Type fetched via getTypeDescendants() doesn't match type fetched via getTypeChildren(): "
                                            + tree.getItem().getId());
                            addResult(assertEquals(compareType, typeChilden.get(compareType.getId()), null, failure));
                        }
                    }
                } catch (CmisObjectNotFoundException e) {
                    addResult(createResult(FAILURE,
                            "Type children fetched via getTypeDescendants() are not available via getTypeChildren(): "
                                    + tree.getItem().getId(), e, false));
                }

                numOfTypes += runTypeChecks(session, tree.getChildren());
            }
        }

        return numOfTypes;
    }

}
