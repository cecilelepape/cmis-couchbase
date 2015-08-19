/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.chemistry.opencmis.jcr.util;

/**
 * Interface for predicates of type <code>T</code>, i.e. functions from
 * <code>T</code> to <code>boolean</code>.
 */
public interface Predicate<T> {

    /**
     * Evaluates the predicate for the given object.
     * 
     * @param t
     *            some object
     * @return predicate result
     */
    boolean evaluate(T t);

    /**
     * Constant predicate that returns <code>true</code> for all objects.
     */
    Predicate<?> TRUE = new Predicate<Object>() {
        public boolean evaluate(Object object) {
            return true;
        }
    };

    /**
     * Constant predicate that returns <code>false</code> for all objects.
     */
    Predicate<?> FALSE = new Predicate<Object>() {
        public boolean evaluate(Object object) {
            return false;
        }
    };

}
