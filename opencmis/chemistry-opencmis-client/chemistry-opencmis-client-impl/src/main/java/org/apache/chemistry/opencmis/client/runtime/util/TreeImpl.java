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
package org.apache.chemistry.opencmis.client.runtime.util;

import java.util.List;

import org.apache.chemistry.opencmis.client.api.Tree;

public class TreeImpl<T> implements Tree<T> {

    private final T item;
    private final List<Tree<T>> children;

    public TreeImpl(T item, List<Tree<T>> children) {
        if (item == null) {
            throw new IllegalArgumentException("Item must be set!");
        }
        this.item = item;
        this.children = children;
    }

    public T getItem() {
        return item;
    }

    public List<Tree<T>> getChildren() {
        return this.children;
    }
}
