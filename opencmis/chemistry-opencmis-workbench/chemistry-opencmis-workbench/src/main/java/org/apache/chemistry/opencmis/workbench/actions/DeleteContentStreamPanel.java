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
package org.apache.chemistry.opencmis.workbench.actions;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.enums.Action;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.apache.chemistry.opencmis.workbench.swing.ActionPanel;

public class DeleteContentStreamPanel extends ActionPanel {

    private static final long serialVersionUID = 1L;

    public DeleteContentStreamPanel(ClientModel model) {
        super("Delete Content Stream", "Delete Content Stream", model);
    }

    @Override
    protected void createActionComponents() {
    }

    @Override
    public boolean isAllowed() {
        if ((getObject() == null) || !(getObject() instanceof Document)) {
            return false;
        }

        if ((getObject().getAllowableActions() == null)
                || (getObject().getAllowableActions().getAllowableActions() == null)) {
            return true;
        }

        return getObject().hasAllowableAction(Action.CAN_DELETE_CONTENT_STREAM);
    }

    @Override
    public boolean doAction() {
        ((Document) getObject()).deleteContentStream(false);
        return true;
    }
}