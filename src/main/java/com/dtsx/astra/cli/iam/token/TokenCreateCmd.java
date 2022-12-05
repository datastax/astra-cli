package com.dtsx.astra.cli.iam.token;

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
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Create a token providing a role identifier.
 */
@Command(name = "create", description = "Display the list of tokens in an organization")
public class TokenCreateCmd extends AbstractConnectedCmd {

    /** Provide a keyspace Name. */
    @Required
    @Option(name = {"-r", "--role" },
            title = "ROLE",
            arity = 1,
            description = "Identifier of the role for this token")
    public String role;

    /** {@inheritDoc} */
    public void execute() {
        ServiceToken.getInstance().createToken(role);
    }

}
