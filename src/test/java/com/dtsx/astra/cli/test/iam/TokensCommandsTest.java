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
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TokensCommandsTest extends AbstractCmdTest {

    @Test
    @Order(1)
    public void should_list_token() {
        assertSuccessCli("token list");
        assertSuccessCli("token list -v");
        assertSuccessCli("token list --no-color");
        assertSuccessCli("token list -o json");
        assertSuccessCli("token list -o csv");
    }

    @Test
    @Order(2)
    public void should_list_token_errors() {
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "token list -w");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "token list DB");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "token coaster");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "token list -o yaml");
    }

    @Test
    @Order(3)
    public void should_create_delete_token() {
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
    @Order(4)
    @DisplayName("Create a token a revoke it")
    public void createAndRevokeTokenTest() {
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
