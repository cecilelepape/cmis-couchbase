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

import static org.apache.chemistry.opencmis.commons.impl.WSConverter.convert;
import static org.apache.chemistry.opencmis.commons.impl.WSConverter.convertExtensionHolder;
import static org.apache.chemistry.opencmis.commons.impl.WSConverter.setExtensionValues;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.MTOM;

import org.apache.chemistry.opencmis.commons.data.ExtensionsData;
import org.apache.chemistry.opencmis.commons.data.ObjectData;
import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisExtensionType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisObjectType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.PolicyServicePort;
import org.apache.chemistry.opencmis.commons.server.CmisService;

/**
 * CMIS Policy Service.
 */
@MTOM
@WebService(endpointInterface = "org.apache.chemistry.opencmis.commons.impl.jaxb.PolicyServicePort")
public class PolicyService extends AbstractService implements PolicyServicePort {
    @Resource
    public WebServiceContext wsContext;

    public void applyPolicy(String repositoryId, String policyId, String objectId, Holder<CmisExtensionType> extension)
            throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            ExtensionsData extData = convertExtensionHolder(extension);

            if (stopBeforeService(service)) {
                return;
            }

            service.applyPolicy(repositoryId, policyId, objectId, extData);

            if (stopAfterService(service)) {
                return;
            }

            setExtensionValues(extData, extension);
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public List<CmisObjectType> getAppliedPolicies(String repositoryId, String objectId, String filter,
            CmisExtensionType extension) throws CmisException {
        CmisService service = null;
        CmisVersion cmisVersion = null;
        try {
            service = getService(wsContext, repositoryId);
            cmisVersion = getCmisVersion(wsContext);

            if (stopBeforeService(service)) {
                return null;
            }

            List<ObjectData> policies = service.getAppliedPolicies(repositoryId, objectId, filter, convert(extension));

            if (stopAfterService(service)) {
                return null;
            }

            if (policies == null) {
                return null;
            }

            List<CmisObjectType> result = new ArrayList<CmisObjectType>();
            for (ObjectData object : policies) {
                result.add(convert(object, cmisVersion));
            }

            return result;
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }

    public void removePolicy(String repositoryId, String policyId, String objectId, Holder<CmisExtensionType> extension)
            throws CmisException {
        CmisService service = null;
        try {
            service = getService(wsContext, repositoryId);

            ExtensionsData extData = convertExtensionHolder(extension);

            if (stopBeforeService(service)) {
                return;
            }

            service.removePolicy(repositoryId, policyId, objectId, extData);

            if (stopAfterService(service)) {
                return;
            }

            setExtensionValues(extData, extension);
        } catch (Exception e) {
            throw convertException(e);
        } finally {
            closeService(service);
        }
    }
}
