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

/**
 * (Taken from JSON.simple <http://code.google.com/p/json-simple/> and modified
 * for OpenCMIS.)
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class Yytoken {
    public static final int TYPE_VALUE = 0;// JSON primitive value:
                                           // string,number,boolean,null
    public static final int TYPE_LEFT_BRACE = 1;
    public static final int TYPE_RIGHT_BRACE = 2;
    public static final int TYPE_LEFT_SQUARE = 3;
    public static final int TYPE_RIGHT_SQUARE = 4;
    public static final int TYPE_COMMA = 5;
    public static final int TYPE_COLON = 6;
    public static final int TYPE_EOF = -1;// end of file

    public int type = 0;
    public Object value = null;

    public Yytoken(int type, Object value) {
        this.type = type;
        this.value = value;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        switch (type) {
        case TYPE_VALUE:
            sb.append("VALUE(").append(value).append(')');
            break;
        case TYPE_LEFT_BRACE:
            sb.append("LEFT BRACE({)");
            break;
        case TYPE_RIGHT_BRACE:
            sb.append("RIGHT BRACE(})");
            break;
        case TYPE_LEFT_SQUARE:
            sb.append("LEFT SQUARE([)");
            break;
        case TYPE_RIGHT_SQUARE:
            sb.append("RIGHT SQUARE(])");
            break;
        case TYPE_COMMA:
            sb.append("COMMA(,)");
            break;
        case TYPE_COLON:
            sb.append("COLON(:)");
            break;
        case TYPE_EOF:
            sb.append("END OF FILE");
            break;
        default:
            assert false;
        }
        return sb.toString();
    }
}
