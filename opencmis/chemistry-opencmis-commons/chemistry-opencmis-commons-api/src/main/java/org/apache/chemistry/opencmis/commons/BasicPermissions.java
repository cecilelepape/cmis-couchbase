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
package org.apache.chemistry.opencmis.commons;

/**
 * CMIS basic permissions constants.
 */
public final class BasicPermissions {

    private BasicPermissions() {
    }

    /** CMIS read permission {@code cmis:read}. */
    public static final String READ = "cmis:read";
    /** CMIS write permission {@code cmis:write}. */
    public static final String WRITE = "cmis:write";
    /** CMIS all permission {@code cmis:all}. */
    public static final String ALL = "cmis:all";
}
