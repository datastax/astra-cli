package com.datastax.astra.cli.org;

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

import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.datastax.astra.cli.db.DatabaseService;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * List regions
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = OrganizationService.CMD_REGIONS_DB_CLASSIC, description = "Show available regions (classic).")
public class OrgListRegionsDbClassicCmd extends AbstractConnectedCmd {

    /**
     * Cloud provider
     */
    @Option(name = { "-c", "--cloud" }, title = "CLOUD", arity = 1,
            description = "Filter on Cloud provider")
    protected String cloud;

    /**
     * Filter on names
     */
    @Option(name = { "-f", "--filter" }, title = "filter", arity = 1,
            description = "Filter on names")
    protected String filter;

    /** {@inheritDoc} */
    public void execute() {
        OrganizationService.getInstance().listRegionsDbClassic(cloud, filter);
    }

}
