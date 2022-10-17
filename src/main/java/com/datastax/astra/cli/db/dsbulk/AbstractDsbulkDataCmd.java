package com.datastax.astra.cli.db.dsbulk;

import com.github.rvesse.airline.annotations.Option;

/**
 * Load/UnLoad data into AstraDB.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class AbstractDsbulkDataCmd extends AbstractDsbulkCmd {
    
    /**
     * Optional filter
     */
    @Option(name = { "-url" },
            title = "url", 
            description = "The URL or path of the resource(s) to read from or write to.")
    protected String url;
    
    /**
     * Optional filter
     */
    @Option(name = { "-delim" },
            title = "delim", 
            description = "The character(s) to use as field delimiter. Field delimiters "
                    + "containing more than one character are accepted.")
    protected String delim = ",";
    
    /**
     * Optional filter
     */
    @Option(name = { "-m", "--schema.mapping" },
            title = "mapping", 
            description = "The field-to-column mapping to use, that applies to both "
                    + "loading and unloading; ignored when counting.")
    protected String mapping;
    
    /**
     * Optional filter
     */
    @Option(name = { "-header" },
            title = "header", 
            description = "Enable or disable whether the files to read "
                    + "or write begin with a header line.")
    protected boolean header = true;
    
    /**
     * Optional filter
     */
    @Option(name = { "-skipRecords" },
            title = "skipRecords", 
            description = "The number of records to skip from each input "
                    + "file before the parser can begin to execute. Note "
                    + "that if the file contains a header line, that line "
                    + "is not counted as a valid record. This setting is "
                    + "ignored when writing.")
    protected int skipRecords = 0;
    
    /**
     * Optional filter
     */
    @Option(name = { "-maxErrors" },
            title = "maxErrors", 
            description = "The maximum number of errors to tolerate before aborting the entire operation.")
    protected int maxErrors = 100;
  
}
