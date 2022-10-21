package com.datastax.astra.cli.test.iam;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import com.datastax.astra.cli.core.ExitCode;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Commands relative to users.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
public class UsersCommandsTest extends AbstractCmdTest {

    @Test
    @Order(1)
    public void should_list_users() {
        assertSuccessCli("user list");
        assertSuccessCli("user list -v");
        assertSuccessCli("user list --no-color");
        assertSuccessCli("user list -o json");
        assertSuccessCli("user list -o csv");
    }
    
    @Test
    @Order(2)
    public void should_list_users_errors() {
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "user list -w");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "user list DB");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "user coaster");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "user list -o yaml");
    }
    
    @Test
    @Order(3)
    public void should_get_user() {
        String USERNAME = "cedrick.lunven@datastax.com";
        assertSuccessCli("user get -v " + USERNAME);
        assertSuccessCli("user get -v " + USERNAME + " -o json");
        assertSuccessCli("user get -v " + USERNAME + " -o csv");
        assertSuccessCli("user get --no-color " + USERNAME);
    }
    
    @Test
    @Order(4)
    public void should_get_user_error() {
        String userInvalidId = "dde8a0e9-f4ae-4b42-b642-9f257436c812";
        String userInvalid = "john.doe@datastax.com";
        assertExitCodeCli(ExitCode.NOT_FOUND, "user get " + userInvalid);
        assertExitCodeCli(ExitCode.NOT_FOUND, "user get " + userInvalidId);
    }
    
    @Test
    @Order(5)
    public void should_create_user() {
        // Given
        String USERNAME = "celphys@gmail.com";
        assertExitCodeCli(ExitCode.NOT_FOUND, "user get " + USERNAME);
        // when
        assertSuccessCli("user invite " + USERNAME);
        // Then
        assertSuccessCli("user get " + USERNAME);
        // Then
        assertExitCodeCli(ExitCode.ALREADY_EXIST, "user invite " + USERNAME);
    }
    
    @Test
    @Order(6)
    public void should_delete_user() {
        // Given
        String USERNAME = "celphys@gmail.com";
        assertSuccessCli("user get " + USERNAME);
        // when
        assertSuccessCli("user delete " + USERNAME);
        // Then
        assertExitCodeCli(ExitCode.NOT_FOUND, "user get " + USERNAME);
    }
    
}
