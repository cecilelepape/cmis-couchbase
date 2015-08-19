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
package org.apache.chemistry.opencmis.commons.spi;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.definitions.TypeDefinition;

/**
 * Extended Repository Service interface.
 * 
 * This interface has NO equivalent in the CMIS specification. It contains
 * repository convenience operations for clients and is built on top of the CMIS
 * specified operations.
 * 
 * This interface need not to be implemented by CMIS servers.
 */
public interface ExtendedRepositoryService {

    /**
     * Gets the definition of the specified object type.
     * 
     * @param repositoryId
     *            the identifier for the repository
     * @param typeId
     *            typeId of an object type specified in the repository
     * @param useCache
     *            specifies whether the type definition should be looked up in
     *            the type definition cache first or not
     */
    TypeDefinition getTypeDefinition(String repositoryId, String typeId, ExtensionsData extension, boolean useCache);
}
