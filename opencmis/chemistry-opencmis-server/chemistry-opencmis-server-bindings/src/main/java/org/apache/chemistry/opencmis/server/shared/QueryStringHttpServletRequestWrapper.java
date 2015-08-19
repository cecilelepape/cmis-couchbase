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

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.chemistry.opencmis.commons.impl.IOUtils;

/**
 * HttpServletRequest wrapper that reads the query string in container
 * independent way and decodes the parameter values with UTF-8.
 */
public class QueryStringHttpServletRequestWrapper extends HttpServletRequestWrapper {

    protected Map<String, String[]> parameters;

    public QueryStringHttpServletRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);

        parameters = new HashMap<String, String[]>();

        // parse query string
        parseFormData(request.getQueryString());
    }

    /**
     * Parses the query string.
     */
    protected final void parseFormData(String queryString) throws IOException {
        if (queryString == null || queryString.length() < 3) {
            return;
        }

        String[] nameValuePairs = queryString.split("&");
        for (String nameValuePair : nameValuePairs) {
            int x = nameValuePair.indexOf('=');
            if (x > 0) {
                String name = IOUtils.decodeURL(nameValuePair.substring(0, x));
                String value = (x == nameValuePair.length() - 1 ? "" : IOUtils
                        .decodeURL(nameValuePair.substring(x + 1)));
                addParameter(name, value);
            } else {
                String name = IOUtils.decodeURL(nameValuePair);
                addParameter(name, (String) null);
            }
        }
    }

    /**
     * Adds a value to a parameter.
     */
    protected final void addParameter(String name, String value) {
        String[] values = parameters.get(name);

        if (values == null) {
            parameters.put(name, new String[] { value });
        } else {
            String[] newValues = new String[values.length + 1];
            System.arraycopy(values, 0, newValues, 0, values.length);
            newValues[newValues.length - 1] = value;
            parameters.put(name, newValues);
        }
    }

    /**
     * Adds an array of values to a parameter.
     */
    protected final void addParameter(String name, String[] additionalValues) {
        String[] values = parameters.get(name);

        if (values == null) {
            parameters.put(name, additionalValues);
        } else {
            String[] newValues = new String[values.length + additionalValues.length];
            System.arraycopy(values, 0, newValues, 0, values.length);
            System.arraycopy(additionalValues, 0, newValues, values.length, additionalValues.length);
            parameters.put(name, newValues);
        }
    }

    @Override
    public final String getParameter(String name) {
        String[] values = parameters.get(name);
        if ((values == null) || (values.length == 0)) {
            return null;
        }

        return values[0];
    }

    @Override
    public final Map<String, String[]> getParameterMap() {
        return parameters;
    }

    @Override
    public final Enumeration<String> getParameterNames() {
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public final String[] getParameterValues(String name) {
        return parameters.get(name);
    }
}
