package com.dtsx.astra.cli.test.iam;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.test.AbstractCmdTest;

/**
 * Testing CRUD for roles.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
class RolesCommandsTest extends AbstractCmdTest {
    
    @Test
    @Order(1)
    void should_list_roles() {
        assertSuccessCli("role list");
        assertSuccessCli("role list -v");
        assertSuccessCli("role list --no-color");
        assertSuccessCli("role list -o json");
        assertSuccessCli("role list -o csv");
    }
    
    @Test
    @Order(2)
    void should_list_roles_errors() {
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "role list -w");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "role list DB");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "role coaster");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "role list -o yaml");
    }
    
    @Test
    @Order(3)
    void should_get_role() {
        String ADMIN_ROLE_ID = "dde8a0e9-f4ae-4b42-b642-9f257436c8da";
        assertSuccessCli("role get -v " + ADMIN_ROLE_ID);
        assertSuccessCli("role get -v " + ADMIN_ROLE_ID + " -o json");
        assertSuccessCli("role get -v " + ADMIN_ROLE_ID + " -o csv");
        assertSuccessCli("role get --no-color " + ADMIN_ROLE_ID);
        assertSuccessCli("role describe -v " + ADMIN_ROLE_ID);
        assertSuccessCli("role describe -v " + ADMIN_ROLE_ID + " -o json");
        assertSuccessCli("role describe -v " + ADMIN_ROLE_ID + " -o csv");
        assertSuccessCli("role describe --no-color " + ADMIN_ROLE_ID);
    }
    
    @Test
    @Order(4)
    void should_get_role_error() {
        String roleInvalidId = "dde8a0e9-f4ae-4b42-b642-9f257436c812";
        String roleInvalid = "jgrkjhlkjrheglkrehgrlejk";
        assertExitCodeCli(ExitCode.NOT_FOUND, "role get " + roleInvalid);
        assertExitCodeCli(ExitCode.NOT_FOUND, "role get " + roleInvalidId);
        assertExitCodeCli(ExitCode.NOT_FOUND, "role describe " + roleInvalid);
        assertExitCodeCli(ExitCode.NOT_FOUND, "role describe " + roleInvalidId);
    }
}
