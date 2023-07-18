package com.dtsx.astra.cli.config;

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

import com.dtsx.astra.cli.core.AbstractCmd;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Class to set a section as default in config file
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(
        name="use",
        description="Make a section the one used by default")
public class ConfigUseCmd extends AbstractCmd {

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
        sectionName = removeQuotesIfAny(sectionName);
        ServiceConfig.assertSectionExist(sectionName);
        ctx().getConfiguration().copySection(sectionName, AstraConfiguration.ASTRARC_DEFAULT);
        ctx().getConfiguration().save();
        AstraCliConsole.outputSuccess("Section '" + sectionName + "' is set as default.");
    }

}
