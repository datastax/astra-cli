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

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exception.TokenNotFoundException;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.core.out.ShellTable;
import com.dtsx.astra.cli.iam.role.AstraToken;
import com.dtsx.astra.cli.iam.role.ServiceRole;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.org.TokensClient;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import com.dtsx.astra.sdk.org.domain.IamToken;
import com.dtsx.astra.sdk.org.domain.Role;
import com.dtsx.astra.sdk.utils.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Work with token.
 * The singleton pattern is validated with a Lazy initialization
 * and a thread safe implementation.
 */
@SuppressWarnings("java:S6548")
public class ServiceToken {

    /** column names. */
    public static final String COL_CLIENT_ID   = "Client Id";

    /** column names. */
    public static final String COL_CLIENT_SECRET   = "Client Secret";

    /** column names. */
    public static final String COL_CLIENT_TOKEN   = "Token";

    /** column names. */
    public static final String COL_ROLES      = "Role";

    /** column names. */
    public static final String COL_GENERATED_ON = "Generated On";

    /**
     * Singleton Pattern
     */
    private static ServiceToken instance;

    /**
     * Default Constructor.
     */
    private ServiceToken() {
    }

    /**
     * Singleton Pattern.
     *
     * @return
     *      instance of the service.
     */
    public static synchronized ServiceToken getInstance() {
        if (null == instance) {
            instance = new ServiceToken();
        }
        return instance;
    }

    /**
     * Access Api devops from context.
     *
     * @return
     *      api devops
     */
    private AstraOpsClient apiDevopsOrg() {
        return CliContext.getInstance().getApiDevops();
    }

    /**
     * Find a token by its id.
     *
     * @param clientId
     *      client identifier.
     * @return
     *      token when exist
     */
    public Optional<IamToken> findToken(String clientId) {
        return apiDevopsOrg()
                      .tokens()
                      .findAll()
                      .filter(t -> t.getClientId().equals(clientId))
                      .findFirst();
    }

    /**
     * Find a token by its id.
     *
     * @param clientId
     *      client identifier.
     * @return
     *      true if token exists
     */
    public boolean tokenExist(String clientId) {
        return findToken(clientId).isPresent();
    }

    /**
     * List tokens of an organization.
     */
    public void listTokens() {
        Map<String, String> roles = new HashMap<>();
        ShellTable sht = new ShellTable();
        sht.addColumn(COL_GENERATED_ON,    15);
        sht.addColumn(COL_CLIENT_ID,    20);
        sht.addColumn(COL_ROLES,    30);
        apiDevopsOrg().tokens().findAll().forEach(tok -> {
            Map<String, String> currentLine = new HashMap<>();
            currentLine.put(COL_GENERATED_ON, tok.getGeneratedOn());
            currentLine.put(COL_CLIENT_ID, tok.getClientId());
            roles.computeIfAbsent(tok.getRoles().get(0), k -> ServiceRole.getInstance().get(k).getName());
            currentLine.put(COL_ROLES, roles.get(tok.getRoles().get(0)));
            sht.getCellValues().add(currentLine);
            // Add multiple lines for a single token if multiple Roles
            if (tok.getRoles().size() > 1) {
                for(int i=1;i< tok.getRoles().size();i++) {
                    Map<String, String> line = new HashMap<>();
                    line.put(COL_GENERATED_ON, "");
                    line.put(COL_CLIENT_ID, "");
                    roles.computeIfAbsent(tok.getRoles().get(0), k -> ServiceRole.getInstance().get(k).getName());
                    line.put(COL_ROLES, roles.get(tok.getRoles().get(0)));
                    sht.getCellValues().add(line);
                }
            }
        });
        AstraCliConsole.printShellTable(sht);
    }

    /**
     * Create a new token with a role.
     *
     * @param role
     *      role asked
     * @return
     *      astra token
     */
    public AstraToken createToken(String role) {
        // Validate that role exists.
        Assert.hasLength(role, "role");
        Role r = ServiceRole.getInstance().get(role);
        CreateTokenResponse iam = apiDevopsOrg().tokens().create(r.getId());
        LoggerShell.success("A new token has been created.");
        AstraToken astraToken = new AstraToken(iam.getClientId(), iam.getSecret(), iam.getToken());
        // Display Created token as a table
        ShellTable sht = ShellTable.propertyTable(15, 40);
        sht.addPropertyRow(COL_CLIENT_ID, astraToken.clientId());
        sht.addPropertyRow(COL_CLIENT_SECRET, astraToken.clientSecret());
        sht.addPropertyRow(COL_CLIENT_TOKEN, astraToken.token());
        AstraCliConsole.printShellTable(sht);
        return astraToken;
    }

    /**
     * Delete a token from its id.
     *
     * @param tokenId
     *          token identifier
     */
    public void deleteToken(String tokenId) {
        Assert.hasLength(tokenId, "tokenId");
        TokensClient tokensClient = apiDevopsOrg().tokens();
        if (tokensClient.exist(tokenId)) {
            tokensClient.delete(tokenId);
            LoggerShell.success("Your token has been deleted.");
        } else {
            throw new TokenNotFoundException(tokenId);
        }
    }

}
