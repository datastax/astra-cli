package com.dtsx.astra.cli.config;

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

import com.dtsx.astra.cli.core.AbstractCmd;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Delete a block in the command.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "delete", description = "Delete section in configuration")
public class ConfigDeleteCmd extends AbstractCmd {
    
    /**
     * Section in configuration file to as default.
     */
    @Required
    @Arguments(
       title = "sectionName", 
       description = "Section in configuration file to as as default.")
    protected String sectionName;
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
        OperationsConfig.assertSectionExist(sectionName);
        ctx().getConfiguration().deleteSection(sectionName);
        ctx().getConfiguration().save();
        AstraCliConsole.outputSuccess("Section '" + sectionName + "' has been deleted.");
    }
}
