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
package org.apache.chemistry.opencmis.server.shared;

import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.chemistry.opencmis.commons.enums.CmisVersion;
import org.apache.chemistry.opencmis.commons.impl.ClassLoaderUtil;
import org.apache.chemistry.opencmis.commons.impl.Constants;
import org.apache.chemistry.opencmis.commons.server.CallContext;
import org.apache.chemistry.opencmis.commons.server.CmisServiceFactory;
import org.apache.chemistry.opencmis.server.impl.CallContextImpl;
import org.apache.chemistry.opencmis.server.impl.CmisRepositoryContextListener;
import org.apache.chemistry.opencmis.server.impl.browser.BrowserCallContextImpl;

public abstract class AbstractCmisHttpServlet extends HttpServlet {

    public static final String PARAM_CALL_CONTEXT_HANDLER = "callContextHandler";
    public static final String PARAM_CMIS_VERSION = "cmisVersion";

    private static final long serialVersionUID = 1L;

    private CmisServiceFactory factory;
    private String binding;
    private CmisVersion cmisVersion;
    private CallContextHandler callContextHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        // initialize the call context handler
        callContextHandler = null;
        String callContextHandlerClass = config.getInitParameter(PARAM_CALL_CONTEXT_HANDLER);
        if (callContextHandlerClass != null) {
            try {
                callContextHandler = (CallContextHandler) ClassLoaderUtil.loadClass(callContextHandlerClass)
                        .newInstance();
            } catch (Exception e) {
                throw new ServletException("Could not load call context handler: " + e, e);
            }
        }

        // get service factory
        factory = (CmisServiceFactory) config.getServletContext().getAttribute(
                CmisRepositoryContextListener.SERVICES_FACTORY);

        if (factory == null) {
            throw new ServletException("Service factory not available! Configuration problem?");
        }
    }

    /**
     * Sets the binding.
     */
    protected void setBinding(String binding) {
        this.binding = binding;
    }

    /**
     * Returns the CMIS version configured for this servlet.
     */
    protected CmisVersion getCmisVersion() {
        return cmisVersion;
    }

    protected void setCmisVersion(CmisVersion cmisVersion) {
        this.cmisVersion = cmisVersion;
    }

    /**
     * Returns the {@link CmisServiceFactory}.
     */
    protected CmisServiceFactory getServiceFactory() {
        return factory;
    }

    /**
     * Return the {@link CallContextHandler}
     */
    protected CallContextHandler getCallContextHandler() {
        return callContextHandler;
    }

    /**
     * Creates a {@link CallContext} object from a servlet request.
     */
    protected CallContext createContext(ServletContext servletContext, HttpServletRequest request,
            HttpServletResponse response, TempStoreOutputStreamFactory streamFactory) {
        String[] pathFragments = HttpUtils.splitPath(request);

        String repositoryId = null;
        if (pathFragments.length > 0) {
            repositoryId = pathFragments[0];
        }

        if (repositoryId == null && CallContext.BINDING_ATOMPUB.equals(binding)) {
            // it's a getRepositories or getRepositoryInfo call
            // getRepositoryInfo has the repository ID in the query parameters
            repositoryId = HttpUtils.getStringParameter(request, Constants.PARAM_REPOSITORY_ID);
        }

        CallContextImpl context = null;

        if (CallContext.BINDING_BROWSER.equals(binding)) {
            context = new BrowserCallContextImpl(binding, cmisVersion, repositoryId, servletContext, request, response,
                    factory, streamFactory);
        } else {
            context = new CallContextImpl(binding, cmisVersion, repositoryId, servletContext, request, response,
                    factory, streamFactory);
        }

        // decode range
        context.setRange(request.getHeader("Range"));

        // get locale
        context.setAcceptLanguage(request.getHeader("Accept-Language"));

        // call call context handler
        if (callContextHandler != null) {
            Map<String, String> callContextMap = callContextHandler.getCallContextMap(request);
            if (callContextMap != null) {
                for (Map.Entry<String, String> e : callContextMap.entrySet()) {
                    context.put(e.getKey(), e.getValue());
                }
            }
        }

        return context;
    }
}
