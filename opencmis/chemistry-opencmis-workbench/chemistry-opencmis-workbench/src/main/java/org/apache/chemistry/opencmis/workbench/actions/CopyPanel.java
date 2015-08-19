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

import java.awt.BorderLayout;
import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.client.runtime.ObjectIdImpl;
import org.apache.chemistry.opencmis.workbench.model.ClientModel;
import org.apache.chemistry.opencmis.workbench.swing.ActionPanel;

public class CopyPanel extends ActionPanel {

    private static final long serialVersionUID = 1L;

    private JTextField targetFolderField;

    public CopyPanel(ClientModel model) {
        super("Copy Object", "Copy", model);
    }

    @Override
    protected void createActionComponents() {
        JPanel targetFolderPanel = new JPanel(new BorderLayout());
        targetFolderPanel.setBackground(Color.WHITE);

        targetFolderPanel.add(new JLabel("Target Folder Id:"), BorderLayout.LINE_START);

        targetFolderField = new JTextField(30);
        targetFolderPanel.add(targetFolderField, BorderLayout.CENTER);

        addActionComponent(targetFolderPanel);
    }

    @Override
    public boolean isAllowed() {
        if ((getObject() == null) || !(getObject() instanceof Document)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean doAction() {
        ObjectId targetFolderId = new ObjectIdImpl(targetFolderField.getText());
        ((Document) getObject()).copy(targetFolderId);
        return true;
    }
}
