package com.dtsx.astra.cli.db.dsbulk;

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
 * Load data into AstraDB.
 */
@Command(name = "unload", 
        description = "Unload data leveraging DSBulk")
public class DbUnLoadCmd extends AbstractDsbulkCmd {

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
            arity = 1,
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

    /** {@inheritDoc} */
    @Override
    public void execute()  {
        // When exporting default location is current path.
        if (url == null) url = "./data" ;

        ServiceDsBulk.getInstance().unload(this);
    }

}
