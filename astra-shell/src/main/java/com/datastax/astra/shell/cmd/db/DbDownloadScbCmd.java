package com.datastax.astra.shell.cmd.db;

import com.datastax.astra.shell.ExitCode;
import com.datastax.astra.shell.cmd.BaseCmd;
import com.datastax.astra.shell.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.shell.exception.DatabaseNotFoundException;
import com.datastax.astra.shell.exception.ParamValidationException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.MutuallyExclusiveWith;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Delete a DB is exist
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = OperationsDb.CMD_DOWNLOAD_SCB, description = "Delete an existing database")
public class DbDownloadScbCmd extends BaseCmd {
    
    /**
     * Database name or identifier
     */
    @Required
    @Arguments(title = "DB", description = "Database name or identifier")
    public String databaseId;
    
    /** Authentication token used if not provided in config. */
    @Option(name = { "-d", "--output-directory" }, title = "Folder to save the cloud secure bundle", 
            description = "Astra uses one secure bundle per database and per region")
    @MutuallyExclusiveWith(tag = "output")
    protected String outputDirectory;
    
    /** Authentication token used if not provided in config. */
    @Option(name = { "-f", "--output-file" }, title = "Name of the file for cloud secure bundle when unique region", 
            description = "Astra uses one secure bundle per database and region")
    @MutuallyExclusiveWith(tag = "output")
    protected String outputFile;
    
    
    /** {@inheritDoc} */
    public ExitCode execute()
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, ParamValidationException {
        return OperationsDb.downloadCloudSecureBundles(databaseId, outputDirectory, outputFile);
    }
    
}
