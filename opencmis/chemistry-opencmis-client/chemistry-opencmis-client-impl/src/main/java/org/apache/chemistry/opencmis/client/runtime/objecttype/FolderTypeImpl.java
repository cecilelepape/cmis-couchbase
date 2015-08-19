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
package org.apache.chemistry.opencmis.client.runtime.objecttype;

import java.util.List;

import org.apache.chemistry.opencmis.client.api.FolderType;
import org.apache.chemistry.opencmis.client.api.ItemIterable;
import org.apache.chemistry.opencmis.client.api.ObjectType;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.Tree;
import org.apache.chemistry.opencmis.commons.definitions.FolderTypeDefinition;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.FolderTypeDefinitionImpl;

/**
 * Folder type.
 */
public class FolderTypeImpl extends FolderTypeDefinitionImpl implements FolderType {

    private static final long serialVersionUID = 1L;

    private final ObjectTypeHelper helper;

    public FolderTypeImpl(Session session, FolderTypeDefinition typeDefinition) {
        assert session != null;
        assert typeDefinition != null;

        initialize(typeDefinition);
        helper = new ObjectTypeHelper(session, this);
    }

    public ObjectType getBaseType() {
        return helper.getBaseType();
    }

    public ItemIterable<ObjectType> getChildren() {
        return helper.getChildren();
    }

    public List<Tree<ObjectType>> getDescendants(int depth) {
        return helper.getDescendants(depth);
    }

    public ObjectType getParentType() {
        return helper.getParentType();
    }

    public boolean isBaseType() {
        return helper.isBaseType();
    }

}
