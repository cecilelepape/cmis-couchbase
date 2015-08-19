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
package org.apache.chemistry.opencmis.server.shared;

import java.io.File;

/**
 * A factory for {@link ThresholdOutputStream} objects.
 * 
 * @deprecated use {@link TempStoreOutputStreamFactory}
 */
public final class ThresholdOutputStreamFactory extends TempStoreOutputStreamFactory {

    private ThresholdOutputStreamFactory(File tempDir, int memoryThreshold, long maxContentSize, boolean encrypt) {
        super(tempDir, memoryThreshold, maxContentSize, encrypt);
    }

    /**
     * Creates a new factory. The parameters are used to create new
     * {@link ThresholdOutputStream} objects.
     * 
     * @param tempDir
     *            temp directory or {@code null} for the default temp directory
     * @param memoryThreshold
     *            memory threshold in bytes
     * @param maxContentSize
     *            max size of the content in bytes (-1 to disable the check)
     * @param encrypt
     *            indicates if temporary files must be encrypted
     */
    public static ThresholdOutputStreamFactory newInstance(File tempDir, int memoryThreshold, long maxContentSize,
            boolean encrypt) {
        return new ThresholdOutputStreamFactory(tempDir, memoryThreshold, maxContentSize, encrypt);
    }

    /**
     * Creates a new {@link ThresholdOutputStream} object.
     */
    @Override
    public ThresholdOutputStream newOutputStream() {
        return new ThresholdOutputStream(getTempDir(), getMemoryThreshold(), getMaxContentSize(), isEncrypted());
    }
}
