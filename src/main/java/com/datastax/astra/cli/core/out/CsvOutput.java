package com.datastax.astra.cli.core.out;


import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.datastax.astra.cli.core.ExitCode;

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
    public static final String ENV_LINE_SEPERATOR = "line.separator";
    
    /** line separator. */
    public static final String LINE_SEPARATOR = System.getProperty(ENV_LINE_SEPERATOR);
    
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
     * Getter accessor for attribute 'headers'.
     *
     * @return
     *       current value of 'headers'
     */
    public List<String> getHeaders() {
        return headers;
    }

    /**
     * Setter accessor for attribute 'headers'.
     * @param headers
     * 		new value for 'headers '
     */
    public void setHeaders(List<String> headers) {
        this.headers = headers;
    }
    
    /**
     * Setter accessor for attribute 'headers'.
     * @param headers
     *      new value for 'headers '
     */
    public void setHeaders(String... headers) {
        this.headers = Arrays.asList(headers);
    }

    /**
     * Getter accessor for attribute 'rows'.
     *
     * @return
     *       current value of 'rows'
     */
    public List<Map<String, String>> getRows() {
        return rows;
    }

    /**
     * Setter accessor for attribute 'rows'.
     * @param rows
     * 		new value for 'rows '
     */
    public void setRows(List<Map<String, String>> rows) {
        this.rows = rows;
    }

}
