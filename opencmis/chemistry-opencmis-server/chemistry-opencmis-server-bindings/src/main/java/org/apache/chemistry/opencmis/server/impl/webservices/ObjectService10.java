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
package org.apache.chemistry.opencmis.server.impl.webservices;

import java.math.BigInteger;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.soap.MTOM;

import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisAccessControlListType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisBulkUpdateType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisContentStreamType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisExtensionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisFaultType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectIdAndChangeTokenType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisPropertiesType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumServiceException;

/**
 * CMIS 1.0 Object Service. Excludes CMIS 1.1 operations.
 */
@MTOM
@WebService(endpointInterface = "org.apache.chemistry.opencmis.server.impl.webservices.ObjectServicePort10")
public class ObjectService10 extends ObjectService implements ObjectServicePort10 {

    @WebMethod(exclude = true)
    public void createItem(String repositoryId, CmisPropertiesType properties, String folderId,
            CmisAccessControlListType addACEs, CmisAccessControlListType removeACEs,
            Holder<CmisExtensionType> extension, Holder<String> objectId) throws CmisException {
        CmisFaultType fault = new CmisFaultType();
        fault.setCode(BigInteger.ZERO);
        fault.setMessage("This is a CMIS 1.0 endpoint.");
        fault.setType(EnumServiceException.NOT_SUPPORTED);

        throw new CmisException(fault.getMessage(), fault);
    }

    @WebMethod(exclude = true)
    public void bulkUpdateProperties(String repositoryId, CmisBulkUpdateType bulkUpdateData,
            Holder<CmisExtensionType> extension, Holder<CmisObjectIdAndChangeTokenType> objectIdAndChangeToken)
            throws CmisException {
        CmisFaultType fault = new CmisFaultType();
        fault.setCode(BigInteger.ZERO);
        fault.setMessage("This is a CMIS 1.0 endpoint.");
        fault.setType(EnumServiceException.NOT_SUPPORTED);

        throw new CmisException(fault.getMessage(), fault);
    }

    @WebMethod(exclude = true)
    public void appendContentStream(String repositoryId, Holder<String> objectId, Boolean isLastChunk,
            Holder<String> changeToken, CmisContentStreamType contentStream, Holder<CmisExtensionType> extension)
            throws CmisException {
        CmisFaultType fault = new CmisFaultType();
        fault.setCode(BigInteger.ZERO);
        fault.setMessage("This is a CMIS 1.0 endpoint.");
        fault.setType(EnumServiceException.NOT_SUPPORTED);

        throw new CmisException(fault.getMessage(), fault);
    }
}
