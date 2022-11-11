package com.dtsx.astra.cli.streaming.pulsarshell;

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

import com.dtsx.astra.cli.core.AbstractConnectedCmd;
import com.dtsx.astra.cli.core.exception.CannotStartProcessException;
import com.dtsx.astra.cli.core.exception.FileSystemException;
import com.dtsx.astra.cli.streaming.ServiceStreaming;
import com.dtsx.astra.cli.streaming.exception.TenantNotFoundException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * This command allows loading data with pulsar-client.
 * 
 * 
 * astra pulsar-shell
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "pulsar-shell", description = "Start pulsar admin against your tenant")
public class PulsarShellCmd extends AbstractConnectedCmd {
    
    /**
     * Database name or identifier
     */
    @Required
    @Arguments(title = "TENANT", description = "Tenant unique name")
    public String tenant;
    
    /** Options. */
    @Option(name = {"-e", "--execute-command" }, title = "command", arity = 1,  
            description = "Execute the statement and quit.")
    protected String execute;
   
    /** Cqlsh Options. */
    @Option(name= {"--fail-on-error"}, 
            description= "If true, the shell will be interrupted if a command throws an exception.")
    protected boolean failOnError = false;
    
    /** Cqlsh Options. */
    @Option(name = {"-f", "--filename" }, title = "FILE", arity = 1,  
            description = "Input filename with a list of commands to be executed. Each command must be separated by a newline.")
    protected String fileName;
    
    /** Cqlsh Options. */
    @Option(name=  {"-np", "--no-progress" }, 
            description= "Display raw output of the commands without the fancy progress visualization. ")
    protected boolean noProgress = false;
    
    /** {@inheritDoc} */
    public void execute() 
    throws TenantNotFoundException, CannotStartProcessException, FileSystemException {
        PulsarShellOptions options = new PulsarShellOptions();
        options.setExecute(execute);
        options.setFailOnError(failOnError);
        options.setFileName(fileName);
        options.setNoProgress(noProgress);
        ServiceStreaming.startPulsarShell(options, tenant);
    }
    
}
