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
package org.apache.chemistry.opencmis.query.example;

import java.util.List;
import java.util.Map;

import org.antlr.runtime.RecognitionException;
import org.apache.chemistry.opencmis.commons.exceptions.CmisInvalidArgumentException;
import org.apache.chemistry.opencmis.server.support.TypeManager;
import org.apache.chemistry.opencmis.server.support.query.CmisSelector;
import org.apache.chemistry.opencmis.server.support.query.FunctionReference;
import org.apache.chemistry.opencmis.server.support.query.QueryObject;
import org.apache.chemistry.opencmis.server.support.query.QueryObject.SortSpec;
import org.apache.chemistry.opencmis.server.support.query.QueryUtilStrict;

/**
 * Main entry point for the parser example. It takes a CMISQL query as input,
 * parses it and generates again CMISQL as output when traversing the tree.
 * Runs standalone without a server. Usually this code would be called from
 * the CMIS Discovery Service in a CMIS server implementation.
 *
 */
public class ExampleQueryProcessor {
    
    public static void main(String[] args) {
        String query = "SELECT cmis:name AS name, cmis:objectTypeId, SCORE() FROM cmis:document WHERE cmis:name LIKE 'My%'";
        ExampleQueryProcessor processor = new ExampleQueryProcessor();
        System.out.println("Original CMISQL query: " + query);
        String response = processor.parseQuery(query);
        System.out.println("Generated CMISQL query: " +  response);
    }
    
    public String parseQuery(String queryString) {
        StringBuffer generatedResponse = new StringBuffer();
        
        // Instantiate a walker traversing the WHERE clause and generating the output string
        ExampleQueryWalker walker = new ExampleQueryWalker();

        // create type definitions, for this example we just create cmis:document
        TypeManager typeManager = ExampleTypeManager.getInstance();

        // create the parser helper class with type manager and walker
        QueryUtilStrict queryUtil= new QueryUtilStrict(queryString, typeManager, walker);
        try {
            // parse the statement, then traverse it using out query walker
            queryUtil.processStatement();
            
            QueryObject qo = queryUtil.getQueryObject();
            
            // The SELECT and FROM part is handled in the QueryObject, we just need to retrieve
            // the results, and generte the SELECT string
            String selFromPart = getSelectFromString(qo);
            generatedResponse.append(selFromPart);
            
            // get the generated string from our query walker and append it
            String whereClause = walker.getResult();
            generatedResponse.append(whereClause);
            
            // The ORDER BY part is handled in the QueryObject, we just need to retrieve
            // the results, and generte the ORDER BY string
            generatedResponse.append(getOrderBy(qo));
            
            // Finally we have the full statement and return is as result
            return generatedResponse.toString();
            
        } catch (RecognitionException e) {
            String message = "Syntax error in query: " + queryUtil.getErrorMessage(e);
            System.out.println(message);
            throw new CmisInvalidArgumentException(message, e);
        }
    }

    private String getSelectFromString(QueryObject qo) {
        StringBuffer result = new StringBuffer();
        result.append("SELECT");
        List<CmisSelector> sels = qo.getSelectReferences();
        boolean first = true;
        for (CmisSelector sel : sels) {
            if (first) {
                first = false;
                result.append(" ");
            } else 
                result.append(", ");

            appendSelector(result, sel);                
        }
        
        result.append(" FROM");
        Map<String, String> froms = qo.getTypes();
        first = true;
        for(String from : froms.keySet()) {
            if (first) {
                first = false;
                result.append(" ");
            } else 
                result.append(", ");
            result.append(from);
        }
        result.append(" ");
        return result.toString();
    }

    private void appendSelector(StringBuffer result, CmisSelector sel) {
        result.append(sel.getName());
        if (sel instanceof FunctionReference)
            result.append("()");
        
        if (null != sel.getAliasName()) {
            result.append(" AS ");
            result.append(sel.getAliasName());
        }
    }
    
    private String getOrderBy(QueryObject qo) {
        List<SortSpec> orderBys = qo.getOrderBys();
        if (null == orderBys || orderBys.size() == 0) 
            return "";
        
        StringBuffer result = new StringBuffer();
        result.append(" ORDER BY");
        boolean first = true;
        for (SortSpec sp : orderBys) {
            if (first) {
                first = false;
                result.append(" ");
            } else 
                result.append(", ");

            CmisSelector sel = sp.getSelector();
            appendSelector(result, sel);
            if (!sp.ascending)
                result.append(" DESC");
        }
        return result.toString();
    }
}
