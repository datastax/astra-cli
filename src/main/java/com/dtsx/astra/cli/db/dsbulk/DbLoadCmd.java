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
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Load data into AstraDB.
 */
@Command(name = "load", description = "Load data leveraging DSBulk")
public class DbLoadCmd extends AbstractDsbulkCmd implements DsBulkParameters {

    /**
     * Optional filter
     */
    @Required
    @Option(name = { PARAM_URL }, title = "url", description = "File location to load data")
    protected String url;

    /**
     * Optional filter
     */
    @Option(name = { PARAM_DELIMITER}, title = "delim", description = " Character(s) use as field delimiter.")
    protected String delim = ",";

    /**
     * Optional filter
     */
    @Option(name = { "-m", "--schema.mapping" }, title = "mapping", description = "Field-to-column mapping to use.")
    protected String mapping;

    /**
     * Optional filter
     */
    @Option(name = { PARAM_HEADER }, title = "header", arity = 1, description = "Read, Write Header in input file")
    protected boolean header = true;

    /**
     * Optional filter
     */
    @Option(name = { PARAM_SKIP_RECORDS }, title = "skipRecords", description = "Lines to skip before readind")
    protected int skipRecords = 0;

    /**
     * Optional filter
     */
    @Option(name = { PARAM_MAX_ERRORS }, title = "maxErrors", description = "Maximum number of errors before aborting the operation.")
    protected int maxErrors = 100;

    /**
     * Optional filter
     */
    @Option(name = { "-dryRun" }, title = "dryRun", description = "Enable or disable dry-run mode.")
    boolean dryRun = false;

    /**
     * Optional filter
     */
    @Option(name = { "--schema.allowMissingFields" },
            title = "allowMissingFields",
            arity = 1,
            description = "Ease the mapping")
    boolean allowMissingFields = false;
    
    /** {@inheritDoc} */
    @Override
    public void execute()  {
        ServiceDsBulk.getInstance().load(this);
    }

}
