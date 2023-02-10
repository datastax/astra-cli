package com.dtsx.astra.cli.db.region;

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

import com.dtsx.astra.cli.core.AbstractConnectedCmd;
import com.dtsx.astra.cli.org.ServiceOrganization;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * List regions.
 */
@Command(name = "list-regions-classic", description = "Show available regions (classic).")
public class DbListRegionsClassicCmd extends AbstractConnectedCmd {

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
        ServiceOrganization.getInstance().listRegionsDbClassic(cloud, filter);
    }

}
