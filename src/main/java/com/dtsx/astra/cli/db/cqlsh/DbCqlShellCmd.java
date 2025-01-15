package com.dtsx.astra.cli.db.cqlsh;

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

import com.dtsx.astra.cli.core.exception.CannotStartProcessException;
import com.dtsx.astra.cli.core.exception.FileSystemException;
import com.dtsx.astra.cli.db.AbstractDatabaseCmd;
import com.dtsx.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Start CqlSh for a DB.
 */
@Command(name = "cqlsh", description = "Start Cqlsh")
public class DbCqlShellCmd extends AbstractDatabaseCmd {
    
    /** Cqlsh Options. */
    @Option(name = { "--version" }, 
            description = "Display information of cqlsh.")
    protected boolean cqlShOptionVersion = false;
    
    /** Cqlsh Options. */
    @Option(name= {"--debug"}, 
            description= "Show additional debugging information.")
    protected boolean cqlShOptionDebug = false;
    
    /** Cqlsh Options. */
    @Option(name = {"--encoding" }, title = "ENCODING", arity = 1,  
            description = "Output encoding. Default encoding: utf8.")
    protected String cqlshOptionEncoding;
    
    /** Cqlsh Options. */
    @Option(name = {"-e", "--execute" }, title = "STATEMENT", arity = 1,  
            description = "Execute the statement and quit.")
    protected String cqlshOptionExecute;
    
    /** Cqlsh Options. */
    @Option(name = {"-f", "--file" }, title = "FILE", arity = 1,  
            description = "Execute commands from a CQL file, then exit.")
    protected String cqlshOptionFile;
    
    /** Cqlsh Options. */
    @Option(name = {"-k", "--keyspace" }, title = "KEYSPACE", arity = 1,  
            description = "Authenticate to the given keyspace.")
    protected String cqlshOptionKeyspace;

    /** Cqlsh Options. */
    @Option(name = { "--connect-timeout" }, title = "TIMEOUT", arity = 1,
            description = "Connection timeout in seconds (default is 10")
    protected int cqlshOptionConnectTimeout = 10;

    /** Cqlsh Options. */
    @Option(name = { "--request-timeout" }, title = "TIMEOUT", arity = 1,
            description = "Request timeout in seconds (default is 20")
    protected int cqlshOptionRequestTimeout = 20;
    
    /** {@inheritDoc}  */
    public void execute() 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException,
            CannotStartProcessException, FileSystemException {

        CqlShellOption options = new CqlShellOption(
                cqlShOptionVersion, cqlShOptionDebug, cqlshOptionEncoding,
                cqlshOptionExecute,cqlshOptionFile,cqlshOptionKeyspace,
                cqlshOptionConnectTimeout, cqlshOptionRequestTimeout);
        ServiceCqlShell.getInstance().run(options, db);
    }
    
}
