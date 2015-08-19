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
package org.apache.chemistry.opencmis.tck.tests.query;

import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.commons.data.RepositoryInfo;
import org.apache.chemistry.opencmis.commons.enums.CapabilityQuery;
import org.apache.chemistry.opencmis.tck.impl.AbstractSessionTest;

public abstract class AbstractQueryTest extends AbstractSessionTest {

    protected boolean supportsQuery(Session session) {
        RepositoryInfo repository = session.getRepositoryInfo();

        if (repository.getCapabilities().getQueryCapability() == null) {
            return false;
        }

        return repository.getCapabilities().getQueryCapability() != CapabilityQuery.NONE;
    }

    protected boolean isFulltextOnly(Session session) {
        RepositoryInfo repository = session.getRepositoryInfo();

        if (repository.getCapabilities().getQueryCapability() == null) {
            return false;
        }

        return repository.getCapabilities().getQueryCapability() == CapabilityQuery.FULLTEXTONLY;
    }
}
