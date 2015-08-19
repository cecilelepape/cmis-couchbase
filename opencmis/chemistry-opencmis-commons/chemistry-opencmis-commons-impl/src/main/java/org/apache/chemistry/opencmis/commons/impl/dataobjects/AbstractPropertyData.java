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
package org.apache.chemistry.opencmis.commons.impl.dataobjects;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.chemistry.opencmis.commons.data.MutablePropertyData;
import org.apache.chemistry.opencmis.commons.data.PropertyDataWithDefinition;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;

/**
 * Abstract property data implementation.
 */
public abstract class AbstractPropertyData<T> extends AbstractExtensionData implements MutablePropertyData<T>,
        PropertyDataWithDefinition<T> {

    private static final long serialVersionUID = 1L;

    private String id;
    private String displayName;
    private String localName;
    private String queryName;
    private PropertyDefinition<T> propDef;

    private List<T> values = Collections.emptyList();

    public PropertyDefinition<T> getPropertyDefinition() {
        return propDef;
    }

    public void setPropertyDefinition(PropertyDefinition<T> propDef) {
        this.propDef = propDef;
        if (propDef != null) {
            this.id = propDef.getId();
            this.displayName = propDef.getDisplayName();
            this.localName = propDef.getLocalName();
            this.queryName = propDef.getQueryName();
        } else {
            this.id = null;
            this.displayName = null;
            this.localName = null;
            this.queryName = null;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getQueryName() {
        return queryName;
    }

    public void setQueryName(String queryName) {
        this.queryName = queryName;
    }

    public List<T> getValues() {
        return values;
    }

    public void setValues(List<T> values) {
        if (values == null) {
            this.values = Collections.emptyList();
        } else {
            this.values = values;
        }
    }

    public void setValue(T value) {
        if (value == null) {
            values = Collections.emptyList();
        } else {
            values = new ArrayList<T>(1);
            values.add(value);
        }
    }

    public T getFirstValue() {
        if (values != null && !values.isEmpty()) {
            return values.get(0);
        }

        return null;
    }

    @Override
    public String toString() {
        return "Property [id=" + id + ", display Name=" + displayName + ", local name=" + localName + ", query name="
                + queryName + ", values=" + values + "]" + super.toString();
    }
}
