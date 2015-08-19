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
package org.apache.chemistry.opencmis.jcr.type;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.data.Properties;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.jcr.JcrDocument;
import org.apache.chemistry.opencmis.jcr.JcrFolder;
import org.apache.chemistry.opencmis.jcr.JcrNode;

/**
 * Implemented by type handlers that provides a type that is or inherits from
 * cmis:document.
 */
public interface JcrDocumentTypeHandler extends JcrTypeHandler {

    JcrDocument getJcrNode(Node node) throws RepositoryException;

    /**
     * See CMIS 1.0 section 2.2.4.1 createDocument
     * 
     * @throws org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException
     * 
     */
    JcrNode createDocument(JcrFolder parentFolder, String name, Properties properties, ContentStream contentStream,
            VersioningState versioningState);
}
