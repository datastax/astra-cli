package com.dtsx.astra.cli.iam.role;

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
import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.JsonOutput;
import com.dtsx.astra.cli.core.out.ShellTable;
import com.dtsx.astra.cli.iam.role.exception.RoleNotFoundException;
import com.dtsx.astra.sdk.AstraOpsClient;
import com.dtsx.astra.sdk.org.domain.Role;
import com.dtsx.astra.sdk.utils.IdUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Hold services to interact with Roles
 * The singleton pattern is validated with a Lazy initialization
 * and a thread safe implementation.
 */
@SuppressWarnings("java:S6548")
public class ServiceRole {

    /** Column name. */
    private static final String COLUMN_ROLE_ID          = "Role Id";
    /** Column name. */
    private static final String COLUMN_ROLE_NAME        = "Role Name";
    /** Column name. */
    private static final String COLUMN_ROLE_DESCRIPTION = "Description";

    /**
     * Singleton Pattern
     */
    private static ServiceRole instance;

    /**
     * Singleton Pattern.
     *
     * @return
     *      instance of the service.
     */
    public static synchronized ServiceRole getInstance() {
        if (null == instance) {
            instance = new ServiceRole();
        }
        return instance;
    }

    /**
     * Default Constructor.
     */
    private ServiceRole() {
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
     * List Roles.
     */
    public void listRoles() {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_ROLE_ID, 37);
        sht.addColumn(COLUMN_ROLE_NAME, 20);
        sht.addColumn(COLUMN_ROLE_DESCRIPTION, 20);
        apiDevopsOrg().roles().findAll().forEach(role -> {
            Map<String, String> rf = new HashMap<>();
            rf.put(COLUMN_ROLE_ID, role.getId());
            rf.put(COLUMN_ROLE_NAME, role.getName());
            rf.put(COLUMN_ROLE_DESCRIPTION, role.getPolicy().getDescription());
            sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
    }

    /**
     * Will find the role or empty.
     *
     * @param role
     *      current role identifier
     * @return
     *      role value
     */
    public Optional<Role> findRole(String role) {
        // Find with name
        Optional<Role> optRole = apiDevopsOrg().roles().findByName(role);
        // Find by id if not found by name
        if (optRole.isEmpty() && IdUtils.isUUID(role)) {
            optRole = apiDevopsOrg().roles().find(role);
        }
        return optRole;
    }

    /**
     * Get value of a role for sure.
     *
     * @param role
     *      current role
     * @return
     *      value for role
     */
    public Role get(String role) {
        return findRole(role).orElseThrow(() -> new RoleNotFoundException(role));
    }

    /**
     * Show Role details.
     *
     * @param role
     *      role name
     * @throws RoleNotFoundException
     *      role has not been found
     */
    public void showRole(String role) throws RoleNotFoundException {
        Role r = get(role);
        ShellTable sht = ShellTable.propertyTable(15, 40);
        sht.addPropertyRow("Identifier",    r.getId());
        sht.addPropertyRow("Name",          r.getName());
        sht.addPropertyRow(COLUMN_ROLE_DESCRIPTION,   r.getPolicy().getDescription());
        sht.addPropertyRow("Effect",        r.getPolicy().getEffect());
        switch (CliContext.getInstance().getOutputFormat()) {
            case CSV -> {
                sht.addPropertyRow("Resources", r.getPolicy().getResources().toString());
                sht.addPropertyRow("Actions", r.getPolicy().getActions().toString());
                AstraCliConsole.printShellTable(sht);
            }
            case JSON ->
                AstraCliConsole.printJson(new JsonOutput<>(ExitCode.SUCCESS, "role get " + role, r));
            case HUMAN -> {
                sht.addPropertyListRows("Resources", r.getPolicy().getResources());
                sht.addPropertyListRows("Actions", r.getPolicy().getActions());
                AstraCliConsole.printShellTable(sht);
            }
        }
    }
}
