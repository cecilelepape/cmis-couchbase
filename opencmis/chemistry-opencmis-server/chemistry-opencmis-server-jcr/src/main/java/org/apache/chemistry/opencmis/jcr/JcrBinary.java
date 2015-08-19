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

package org.apache.chemistry.opencmis.jcr;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import javax.jcr.Binary;
import javax.jcr.RepositoryException;

/**
 * <code>JcrBinary</code> implements the JCR <code>Binary</code> interface. This
 * is mostly a copy from org.apache.jackrabbit.value.BinaryImpl in Apache
 * Jackrabbit's jcr-commons module.
 */
public class JcrBinary implements Binary {

    /**
     * empty array
     */
    private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * max size for keeping tmp data in memory
     */
    private static final int MAX_BUFFER_SIZE = 0x10000;

    /**
     * underlying tmp file
     */
    private final File tmpFile;

    /**
     * buffer for small-sized data
     */
    private byte[] buffer = EMPTY_BYTE_ARRAY;

    public static final Binary EMPTY = new JcrBinary(EMPTY_BYTE_ARRAY);

    /**
     * Creates a new <code>JcrBinary</code> instance from an
     * <code>InputStream</code>. The contents of the stream is spooled to a
     * temporary file or to a byte buffer if its size is smaller than
     * {@link #MAX_BUFFER_SIZE}. The input stream is closed by this
     * implementation.
     * 
     * @param in
     *            stream to be represented as a <code>JcrBinary</code> instance
     * @throws IOException
     *             if an error occurs while reading from the stream or writing
     *             to the temporary file
     */
    public JcrBinary(InputStream in) throws IOException {
        byte[] spoolBuffer = new byte[0x2000];
        int read;
        int len = 0;
        OutputStream out = null;
        File spoolFile = null;
        try {
            while ((read = in.read(spoolBuffer)) > 0) {
                if (out != null) {
                    // spool to temp file
                    out.write(spoolBuffer, 0, read);
                    len += read;
                } else if (len + read > MAX_BUFFER_SIZE) {
                    // threshold for keeping data in memory exceeded;
                    // create temp file and spool buffer contents

                    spoolFile = File.createTempFile("bin", null, null);
                    out = new FileOutputStream(spoolFile);
                    out.write(buffer, 0, len);
                    out.write(spoolBuffer, 0, read);
                    buffer = null;
                    len += read;
                } else {
                    // reallocate new buffer and spool old buffer contents
                    byte[] newBuffer = new byte[len + read];
                    System.arraycopy(buffer, 0, newBuffer, 0, len);
                    System.arraycopy(spoolBuffer, 0, newBuffer, len, read);
                    buffer = newBuffer;
                    len += read;
                }
            }
        } finally {
            in.close();
            if (out != null) {
                out.close();
            }
        }

        // init fields
        tmpFile = spoolFile;
    }

    /**
     * Creates a new <code>JcrBinary</code> instance from a <code>byte[]</code>
     * array.
     * 
     * @param buffer
     *            byte array to be represented as a <code>JcrBinary</code>
     *            instance
     */
    public JcrBinary(byte[] buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException("buffer must be non-null");
        }
        this.buffer = buffer;
        tmpFile = null;
    }

    public InputStream getStream() throws RepositoryException {
        if (tmpFile != null) {
            try {
                // this instance is backed by a temp file
                return new FileInputStream(tmpFile);
            } catch (FileNotFoundException e) {
                throw new RepositoryException("already disposed");
            }
        } else {
            // this instance is backed by an in-memory buffer
            return new ByteArrayInputStream(buffer);
        }
    }

    public int read(byte[] b, long position) throws IOException, RepositoryException {
        if (tmpFile != null) {
            // this instance is backed by a temp file
            RandomAccessFile raf = new RandomAccessFile(tmpFile, "r");
            try {
                raf.seek(position);
                return raf.read(b);
            } finally {
                raf.close();
            }
        } else {
            // this instance is backed by an in-memory buffer
            int length = Math.min(b.length, buffer.length - (int) position);
            if (length > 0) {
                System.arraycopy(buffer, (int) position, b, 0, length);
                return length;
            } else {
                return -1;
            }
        }
    }

    public long getSize() throws RepositoryException {
        if (tmpFile != null) {
            // this instance is backed by a temp file
            return tmpFile.exists() ? tmpFile.length() : -1;
        } else {
            // this instance is backed by an in-memory buffer
            return buffer.length;
        }
    }

    public void dispose() {
        if (tmpFile != null) {
            // this instance is backed by a temp file
            tmpFile.delete();
        } else {
            // this instance is backed by an in-memory buffer
            buffer = EMPTY_BYTE_ARRAY;
        }
    }

}
