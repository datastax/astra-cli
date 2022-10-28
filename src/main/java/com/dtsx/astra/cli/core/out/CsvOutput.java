package com.dtsx.astra.cli.core.out;

/*-
 * #%L
 * Astra Cli
 * %%
 * Copyright (C) 2022 DataStax
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dtsx.astra.cli.core.ExitCode;

/**
 * Show cli output as CSV.
 * @author Cedrick LUNVEN (@clunven)
 */
public class CsvOutput implements Serializable{

    /** Serial. */
    @Serial
    private static final long serialVersionUID = -6255622898821481245L;
    
    /** code. */
    public static final String ERROR_CODE_COLUMN    = "error_code";
    
    /** message. */
    public static final String ERROR_MESSAGE_COLUMN = "error_message";
    
    /** line separator. */
    public static final String ENV_LINE_SEPARATOR = "line.separator";
    
    /** line separator. */
    public static final String LINE_SEPARATOR = System.getProperty(ENV_LINE_SEPARATOR);
    
    /**
     * Headers in CSV
     */
    private List < String > headers = new ArrayList<>();
    
    /**
     * Rows in CSV
     */
    private List<Map<String, String >> rows = new ArrayList<>();

    /**
     * Show errors as CSV.
     * 
     * @param errorCode
     *      error code
     * @param errorMessage
     *      error message
     */
    public CsvOutput(ExitCode errorCode, String errorMessage) {
       setHeaders(ERROR_CODE_COLUMN, ERROR_MESSAGE_COLUMN);
       Map<String, String > error = new HashMap<>();
       error.put(ERROR_CODE_COLUMN, String.valueOf(errorCode.getCode()));
       error.put(ERROR_MESSAGE_COLUMN, errorMessage);
       rows.add(error);
    }

    /**
     * Show data as CSV.
     *
     * @param headers
     *      target headers
     * @param rows
     *      list of rows
     */
    public CsvOutput(List< String > headers, List<Map<String, String >> rows) {
        this.rows    = rows;
        this.headers = headers;
    }
    
    /**
     * Escape String to fit csv;
     * 
     * @param s
     *      data to escape
     * @return
     *      escaped data
     */
    protected String csvEscape(String s) {
        if (s==null) {
          return "";
        } else if (s.contains("\"")) {
          return s.substring(0,s.indexOf("\"")) + "\"" + csvEscape(s.substring(s.indexOf("\"")+1));
        } else if (s.contains(",")) {
          return "\""+s+"\"";
        }
        return s;
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        StringBuilder csv = new StringBuilder(String.join(",",headers));
        csv.append(LINE_SEPARATOR);
        rows.forEach(row -> {
            List<String> values = new ArrayList<>();
            for (String colName : headers) {
                values.add(row.containsKey(colName) ? csvEscape(row.get(colName)) : "");
            }
            csv.append(String.join(",",values));
            csv.append(LINE_SEPARATOR);
        });
        return csv.toString();
    }
    
    /**
     * Setter accessor for attribute 'headers'.
     * @param headers
     *      new value for 'headers '
     */
    public void setHeaders(String... headers) {
        this.headers = Arrays.asList(headers);
    }
}
