package com.dtsx.astra.cli.db;

/*-
 * #%L
 * Astra CLI
 * --
 * Copyright (C) 2022 - 2023 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Delete a DB is exist.
 */
@Command(name = "download-scb", description = "Download secure connect bundle archive for a region")
public class DbDownloadScbCmd extends AbstractDatabaseCmd {

    /**
     * Specified a region explicitly
     */
    @Option(name = { "-r", "--region" }, title = "DB_REGION", arity = 1,
            description = "Cloud provider region")
    protected String region;
    
    /** Authentication token used if not provided in config. */
    @Option(name = { "-f", "--output-file" },
            title = "DEST", 
            description = "Destination file")
    protected String destination;
    
    /** {@inheritDoc} */
    public void execute() {
        DaoDatabase.getInstance().downloadCloudSecureBundle(db, region, destination);
    }
    
}
