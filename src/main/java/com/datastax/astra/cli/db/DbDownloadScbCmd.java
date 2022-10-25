package com.datastax.astra.cli.db;

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
