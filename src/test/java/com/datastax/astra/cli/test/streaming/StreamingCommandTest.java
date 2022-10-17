package com.datastax.astra.cli.test.streaming;

import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.datastax.astra.cli.core.ExitCode;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Testing Streaming commands
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
public class StreamingCommandTest extends AbstractCmdTest {
    
    static String RANDOM_TENANT = "cli-" + 
            UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
    
    @Test
    @Order(1)
    public void should_show_help() {
        assertSuccessCli("help");
        assertSuccessCli("help streaming");
        assertSuccessCli("help streaming create");
        assertSuccessCli("help streaming delete");
        assertSuccessCli("help streaming list");
        assertSuccessCli("help streaming pulsar-shell");
        assertSuccessCli("help streaming pulsar-token");
        assertSuccessCli("help streaming exist");
        assertSuccessCli("help streaming status");
    }
    
    @Test
    @Order(2)
    public void should_create_tenant() {
        // Given
        Assertions.assertFalse(ctx().getApiDevopsStreaming().tenant(RANDOM_TENANT).exist());
        // When
        assertSuccessCli("streaming exist " + RANDOM_TENANT);
        assertSuccessCli("streaming create " + RANDOM_TENANT);
        // Then
        Assertions.assertTrue(ctx().getApiDevopsStreaming().tenant(RANDOM_TENANT).exist());
        assertExitCodeCli(ExitCode.ALREADY_EXIST, "streaming create " + RANDOM_TENANT);
    }
    
    @Test
    @Order(3)
    public void should_list_tenants() {
        assertSuccessCli("streaming list");
        assertSuccessCli("streaming list -v");
        assertSuccessCli("streaming list --no-color");
        assertSuccessCli("streaming list -o json");
        assertSuccessCli("streaming list -o csv");
    }
    
    @Test
    @Order(4)
    public void should_list_tenants_errors() {
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "streaming list -w");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "streaming list DB");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "streaming coaster");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "streaming list -o yaml");
    }
    
    @Test
    @Order(5)
    public void should_get_tenant() {
        assertSuccessCli("streaming get " + RANDOM_TENANT);
        assertSuccessCli("streaming get " + RANDOM_TENANT + " -o json");
        assertSuccessCli("streaming get " + RANDOM_TENANT + " -o csv");
        assertSuccessCli("streaming get " + RANDOM_TENANT + " --key status");
        assertSuccessCli("streaming get " + RANDOM_TENANT + " --key cloud");
        assertSuccessCli("streaming get " + RANDOM_TENANT + " --key pulsar_token");
        assertSuccessCli("streaming get " + RANDOM_TENANT + " --key region");
        assertSuccessCli("streaming exist " + RANDOM_TENANT);
        assertSuccessCli("streaming status " + RANDOM_TENANT);
        assertSuccessCli("streaming pulsar-token " + RANDOM_TENANT);
    }
    
    @Test
    @Order(6)
    public void should_get_tenant_errors() {
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "streaming invalid");
        assertExitCodeCli(ExitCode.NOT_FOUND, "streaming get " + RANDOM_TENANT + " --invalid");
        assertExitCodeCli(ExitCode.NOT_FOUND, "streaming get does-not-exist");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "streaming get " + RANDOM_TENANT + " -o yaml"); 
    }
    
    @Test
    @Order(7)
    public void should_delete_tenant() {
        // Given
        Assertions.assertTrue(ctx().getApiDevopsStreaming().tenant(RANDOM_TENANT).exist());
        // When
        assertSuccessCli("streaming delete " + RANDOM_TENANT);
        // Then
        assertSuccessCli("streaming exist " + RANDOM_TENANT);
        Assertions.assertFalse(ctx().getApiDevopsStreaming().tenant(RANDOM_TENANT).exist());
    }
    
    @Test
    @Order(8)
    public void should_delete_tenant_error() {
        assertExitCodeCli(ExitCode.NOT_FOUND, "streaming delete does-not-exist");
    }
}
