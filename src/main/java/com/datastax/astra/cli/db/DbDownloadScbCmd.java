package com.datastax.astra.cli.db;

import com.datastax.astra.cli.core.exception.InvalidArgumentException;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Delete a DB is exist
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "download-scb", description = "Delete an existing database")
public class DbDownloadScbCmd extends AbstractDatabaseCmd {
   
    /**
     * Cloud provider region to provision
     */
    @Option(name = { "-r", "--region" }, 
            title = "REGION", 
            description = "Cloud provider region")
    protected String region;
    
    /** Authentication token used if not provided in config. */
    @Option(name = { "-f", "--output-file" },
            title = "DEST", 
            description = "Destination file")
    protected String destination;
    
    /** {@inheritDoc} */
    public void execute()
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, InvalidArgumentException {
        DatabaseDao.getInstance()
                   .downloadCloudSecureBundle(db, region, destination);
    }
    
}
