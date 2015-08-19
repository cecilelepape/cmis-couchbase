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
package org.apache.chemistry.opencmis.commons.data;

import java.util.GregorianCalendar;

/**
 * Content stream with HTTP cache headers. (AtomPub binding and Browser binding
 * server only.)
 */
public interface CacheHeaderContentStream extends ContentStream {

    /**
     * Returns the Cache-Control header.
     * 
     * @return the value of the Cache-Control header or {@code null} if the
     *         header should not be set
     */
    String getCacheControl();

    /**
     * Returns the ETag header.
     * 
     * @return the value of the ETag header or {@code null} if the header
     *         should not be set
     */
    String getETag();

    /**
     * Returns the Expires header.
     * 
     * @return the value of the Expires header or {@code null} if the header
     *         should not be set
     */
    GregorianCalendar getExpires();
}
