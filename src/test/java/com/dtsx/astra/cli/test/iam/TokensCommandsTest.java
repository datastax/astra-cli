package com.dtsx.astra.cli.test.iam;

import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.iam.role.AstraToken;
import com.dtsx.astra.cli.iam.role.ServiceRole;
import com.dtsx.astra.cli.iam.token.ServiceToken;
import com.dtsx.astra.cli.test.AbstractCmdTest;
import com.dtsx.astra.sdk.org.domain.Role;
import org.junit.jupiter.api.*;

/**
 * Testing CRUD for tokens.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TokensCommandsTest extends AbstractCmdTest {

    @Test
    @Order(1)
    void should_show_token() {
        assertSuccessCli("token");
        assertSuccessCli("token get");
    }

    @Test
    @Order(2)
    void should_list_token() {
        assertSuccessCli("token list");
        assertSuccessCli("token list -v");
        assertSuccessCli("token list --no-color");
        assertSuccessCli("token list -o json");
        assertSuccessCli("token list -o csv");
    }

    @Test
    @Order(3)
    void should_list_token_errors() {
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "token list -w");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "token list DB");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "token coaster");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "token list -o yaml");
    }

    @Test
    @Order(4)
    void should_create_delete_token() {
        // Given
        assertSuccessCli("token list");
        Role role = ServiceRole.getInstance().get("Database Administrator");
        // When
        assertSuccessCli("token create -r " + role.getId());
        AstraToken token = ServiceToken.getInstance().createToken(role.getId());
        // Then
        Assertions.assertTrue(ServiceToken.getInstance().tokenExist(token.clientId()));
        // When
        assertSuccessCli("token delete " + token.clientId());
        // Then
        Assertions.assertFalse(ServiceToken.getInstance().tokenExist(token.clientId()));
    }

    @Test
    @Order(5)
    @DisplayName("Create a token a revoke it")
    void createAndRevokeTokenTest() {
        // Given
        assertSuccessCli("token list");
        Role role = ServiceRole.getInstance().get("Database Administrator");
        // When
        assertSuccessCli("token create -r " + role.getId());
        AstraToken token = ServiceToken.getInstance().createToken(role.getId());
        // Then
        Assertions.assertTrue(ServiceToken.getInstance().tokenExist(token.clientId()));
        // When
        assertSuccessCli("token revoke " + token.clientId());
        // Then
        Assertions.assertFalse(ServiceToken.getInstance().tokenExist(token.clientId()));
    }
}
