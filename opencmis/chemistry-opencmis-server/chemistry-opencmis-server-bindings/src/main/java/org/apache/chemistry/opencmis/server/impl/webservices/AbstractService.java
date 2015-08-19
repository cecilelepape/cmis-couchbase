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

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.exceptions.CmisBaseException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisConstraintException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisContentAlreadyExistsException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisFilterNotValidException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNameConstraintViolationException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisObjectNotFoundException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisRuntimeException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStorageException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisStreamNotSupportedException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisUpdateConflictException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisVersioningException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisException;
import org.apache.chemistry.opencmis.commons.impl.jaxb.CmisFaultType;
import org.apache.chemistry.opencmis.commons.impl.jaxb.EnumServiceException;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisService;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.commons.server.ProgressControlCmisService;
import org.apache.chemistry.opencmis.commons.server.ProgressControlCmisService.Progress;
import org.apache.chemistry.opencmis.server.impl.CallContextImpl;
import org.apache.chemistry.opencmis.server.impl.CmisRepositoryContextListener;
import org.apache.chemistry.opencmis.server.impl.ServerVersion;
import org.apache.chemistry.opencmis.server.shared.ExceptionHelper;
import org.apache.chemistry.opencmis.server.shared.TempStoreOutputStreamFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * This class contains operations used by all services.
 */
public abstract class AbstractService {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractService.class);

    public static final String CALL_CONTEXT_MAP = "org.apache.chemistry.opencmis.callcontext";

    /**
     * Returns the services factory.
     */
    protected CmisServiceFactory getServiceFactory(WebServiceContext wsContext) {
        ServletContext servletContext = (ServletContext) wsContext.getMessageContext().get(
                MessageContext.SERVLET_CONTEXT);

        // get services factory
        CmisServiceFactory factory = (CmisServiceFactory) servletContext
                .getAttribute(CmisRepositoryContextListener.SERVICES_FACTORY);

        if (factory == null) {
            throw new CmisRuntimeException("Service factory not available! Configuration problem?");
        }

        HttpServletResponse httpResp = (HttpServletResponse) wsContext.getMessageContext().get(
                MessageContext.SERVLET_RESPONSE);
        httpResp.setHeader("Server", ServerVersion.OPENCMIS_SERVER);

        return factory;
    }

    /**
     * Creates a CallContext object for the Web Service context.
     */
    @SuppressWarnings("unchecked")
    protected CallContext createContext(WebServiceContext wsContext, CmisServiceFactory factory, String repositoryId) {
        ServletContext servletContext = (ServletContext) wsContext.getMessageContext().get(
                MessageContext.SERVLET_CONTEXT);
        HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(
                MessageContext.SERVLET_REQUEST);
        HttpServletResponse response = (HttpServletResponse) wsContext.getMessageContext().get(
                MessageContext.SERVLET_RESPONSE);

        CmisVersion cmisVersion = (CmisVersion) request.getAttribute(CmisWebServicesServlet.CMIS_VERSION);
        if (cmisVersion == null) {
            throw new CmisRuntimeException("Server configuration issue. CMIS version not set!");
        }

        TempStoreOutputStreamFactory streamFactoy = TempStoreOutputStreamFactory.newInstance(factory, repositoryId);
        CallContextImpl context = new CallContextImpl(CallContext.BINDING_WEBSERVICES, cmisVersion, repositoryId,
                servletContext, request, response, factory, streamFactoy);

        Map<String, List<String>> headers = (Map<String, List<String>>) wsContext.getMessageContext().get(
                MessageContext.HTTP_REQUEST_HEADERS);
        if (headers != null) {
            for (Map.Entry<String, List<String>> header : headers.entrySet()) {
                if (header.getKey().equalsIgnoreCase("Accept-Language") && header.getValue() != null
                        && !header.getValue().isEmpty()) {
                    context.setAcceptLanguage(header.getValue().get(0));
                    break;
                }
            }
        }

        MessageContext mc = wsContext.getMessageContext();
        Map<String, String> callContextMap = (Map<String, String>) mc.get(CALL_CONTEXT_MAP);
        if (callContextMap != null) {
            for (Map.Entry<String, String> e : callContextMap.entrySet()) {
                context.put(e.getKey(), e.getValue());
            }
        }

        return context;
    }

    /**
     * Returns the CMIS version.
     */
    protected CmisVersion getCmisVersion(WebServiceContext wsContext) {
        HttpServletRequest request = (HttpServletRequest) wsContext.getMessageContext().get(
                MessageContext.SERVLET_REQUEST);
        return (CmisVersion) request.getAttribute(CmisWebServicesServlet.CMIS_VERSION);
    }

    /**
     * Returns the {@link CmisService} object.
     */
    protected CmisService getService(WebServiceContext wsContext, String repositoryId) {
        CmisServiceFactory factory = getServiceFactory(wsContext);
        CallContext context = createContext(wsContext, factory, repositoryId);
        return factory.getService(context);
    }

    /**
     * Determines if the processing should be stopped before the service method
     * is called.
     * 
     * @return {@code true} if the processing should be stopped, {@code false}
     *         otherwise
     */
    protected boolean stopBeforeService(CmisService service) {
        if (!(service instanceof ProgressControlCmisService)) {
            return false;
        }

        return ((ProgressControlCmisService) service).beforeServiceCall() == Progress.STOP;
    }

    /**
     * Determines if the processing should be stopped after the service method
     * is called.
     * 
     * @return {@code true} if the processing should be stopped, {@code false}
     *         otherwise
     */
    protected boolean stopAfterService(CmisService service) {
        if (!(service instanceof ProgressControlCmisService)) {
            return false;
        }

        return ((ProgressControlCmisService) service).afterServiceCall() == Progress.STOP;
    }

    /**
     * Closes the service instance.
     */
    protected void closeService(CmisService service) {
        if (service != null) {
            service.close();
        }
    }

    /**
     * Converts a CMIS exception to the appropriate Web Service exception.
     */
    protected CmisException convertException(Exception ex) {
        CmisFaultType fault = new CmisFaultType();
        fault.setMessage("Unknown exception");
        fault.setCode(BigInteger.ZERO);
        fault.setType(EnumServiceException.RUNTIME);

        if (ex != null) {
            if (ex instanceof CmisBaseException) {
                fault.setCode(((CmisBaseException) ex).getCode());
                fault.setMessage(ex.getMessage());

                if (ex instanceof CmisConstraintException) {
                    fault.setType(EnumServiceException.CONSTRAINT);
                } else if (ex instanceof CmisContentAlreadyExistsException) {
                    fault.setType(EnumServiceException.CONTENT_ALREADY_EXISTS);
                } else if (ex instanceof CmisFilterNotValidException) {
                    fault.setType(EnumServiceException.FILTER_NOT_VALID);
                } else if (ex instanceof CmisInvalidArgumentException) {
                    fault.setType(EnumServiceException.INVALID_ARGUMENT);
                } else if (ex instanceof CmisNameConstraintViolationException) {
                    fault.setType(EnumServiceException.NAME_CONSTRAINT_VIOLATION);
                } else if (ex instanceof CmisNotSupportedException) {
                    fault.setType(EnumServiceException.NOT_SUPPORTED);
                } else if (ex instanceof CmisObjectNotFoundException) {
                    fault.setType(EnumServiceException.OBJECT_NOT_FOUND);
                } else if (ex instanceof CmisPermissionDeniedException) {
                    fault.setType(EnumServiceException.PERMISSION_DENIED);
                } else if (ex instanceof CmisStorageException) {
                    LOG.error(ex.getMessage(), ex);
                    fault.setType(EnumServiceException.STORAGE);
                } else if (ex instanceof CmisStreamNotSupportedException) {
                    fault.setType(EnumServiceException.STREAM_NOT_SUPPORTED);
                } else if (ex instanceof CmisUpdateConflictException) {
                    fault.setType(EnumServiceException.UPDATE_CONFLICT);
                } else if (ex instanceof CmisVersioningException) {
                    fault.setType(EnumServiceException.VERSIONING);
                } else {
                    LOG.error(ex.getMessage(), ex);
                }
            } else {
                fault.setMessage("An error occurred!");

                if (ex instanceof IOException) {
                    LOG.warn(ex.getMessage(), ex);
                } else {
                    LOG.error(ex.getMessage(), ex);
                }
            }

            Node node = ExceptionHelper.getStacktraceAsNode(ex);
            if (node != null) {
                fault.getAny().add(node);
            }
        }

        return new CmisException(fault.getMessage(), fault, ex);
    }
}
