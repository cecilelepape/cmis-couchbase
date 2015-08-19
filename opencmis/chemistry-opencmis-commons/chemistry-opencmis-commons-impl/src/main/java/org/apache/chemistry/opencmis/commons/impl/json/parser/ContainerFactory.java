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
package org.apache.chemistry.opencmis.commons.impl.json.parser;

import java.util.List;
import java.util.Map;

/**
 * Container factory for creating containers for JSON object and JSON array.
 * 
 * (Taken from JSON.simple <http://code.google.com/p/json-simple/> and modified
 * for OpenCMIS.)
 * 
 * @see JSONParser#parse(java.io.Reader, ContainerFactory)
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public interface ContainerFactory {
    /**
     * @return A Map instance to store JSON object, or null if you want to use
     *         JSONObject.
     */
    Map<String, Object> createObjectContainer();

    /**
     * @return A List instance to store JSON array, or null if you want to use
     *         JSONArray.
     */
    List<Object> creatArrayContainer();
}
