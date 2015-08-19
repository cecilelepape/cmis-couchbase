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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class NoBodyHttpServletResponseWrapper extends HttpServletResponseWrapper {

    private final NoBodyOutputStream noBodyStream;
    private PrintWriter writer;

    public NoBodyHttpServletResponseWrapper(HttpServletResponse response) throws IOException {
        super(response);
        noBodyStream = new NoBodyOutputStream();
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return noBodyStream;
    }

    public PrintWriter getWriter() throws UnsupportedEncodingException {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(noBodyStream, getCharacterEncoding()));
        }

        return writer;
    }

    private static class NoBodyOutputStream extends ServletOutputStream {
        @Override
        public void write(int b) throws IOException {
            // ignore
        }

        @Override
        public void write(byte[] b) throws IOException {
            // ignore
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            // ignore
        }
    }
}
