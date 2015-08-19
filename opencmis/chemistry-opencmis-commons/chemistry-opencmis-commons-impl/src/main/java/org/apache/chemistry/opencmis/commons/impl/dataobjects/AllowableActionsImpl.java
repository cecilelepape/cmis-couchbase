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

import java.util.EnumSet;
import java.util.Set;

import org.apache.chemistry.opencmis.commons.data.AllowableActions;
import org.apache.chemistry.opencmis.commons.enums.Action;

/**
 * Allowable Actions.
 */
public class AllowableActionsImpl extends AbstractExtensionData implements AllowableActions {

    private static final long serialVersionUID = 1L;

    private Set<Action> allowableActions;

    public Set<Action> getAllowableActions() {
        if (allowableActions == null) {
            allowableActions = EnumSet.noneOf(Action.class);
        }

        return allowableActions;
    }

    public void setAllowableActions(Set<Action> allowableActions) {
        this.allowableActions = allowableActions;
    }

    @Override
    public String toString() {
        return "Allowable Actions [allowable actions=" + allowableActions + "]" + super.toString();
    }
}
