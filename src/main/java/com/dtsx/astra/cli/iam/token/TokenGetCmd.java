package com.dtsx.astra.cli.iam.token;

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

import com.dtsx.astra.cli.config.AstraConfiguration;
import com.dtsx.astra.cli.config.ServiceConfig;
import com.dtsx.astra.cli.core.AbstractConnectedCmd;
import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.core.exception.ConfigurationException;
import com.dtsx.astra.cli.core.exception.TokenNotFoundException;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.sdk.utils.AstraRc;
import com.github.rvesse.airline.annotations.Command;

import java.util.Optional;

/**
 * List tokens of an Organization.
 */
@Command(name = "get", description = "Show current token")
public class TokenGetCmd extends AbstractConnectedCmd {

    /** {@inheritDoc} */
    public void execute() {
        ServiceConfig.assertSectionExist(AstraConfiguration.ASTRARC_DEFAULT);
        Optional<String> optKey = ctx()
                .getConfiguration()
                .getSectionKey(AstraConfiguration.ASTRARC_DEFAULT, AstraRc.ASTRA_DB_APPLICATION_TOKEN);
        if (optKey.isEmpty()) {
            AstraCliConsole.outputError(ExitCode.INVALID_PARAMETER,"Token not found");
            throw new TokenNotFoundException();
        }
        AstraCliConsole.println(optKey.get());
    }

}
