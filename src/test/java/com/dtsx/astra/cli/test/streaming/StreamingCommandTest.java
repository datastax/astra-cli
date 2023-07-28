package com.dtsx.astra.cli.test.streaming;

import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.test.AbstractCmdTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.File;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

/**
 * Testing Streaming commands
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
class StreamingCommandTest extends AbstractCmdTest {
    
    static String RANDOM_TENANT = "cli-" +
            UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);

    @BeforeAll
    static void shouldCreateTemporaryTenantOnce() {
        assertSuccessCli("streaming create %s ".formatted(RANDOM_TENANT));
        await("Tenant Creation")
           .atMost(1, SECONDS)
           .until(() -> ctx().getApiDevopsStreaming().exist(RANDOM_TENANT));
    }

    @Test
    @Order(1)
    void shouldShowHelpTest() {
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
    void shouldExistTenantTest() {
        // When
        assertSuccessCli("streaming exist " + RANDOM_TENANT);
        // Then
        Assertions.assertTrue(ctx().getApiDevopsStreaming().exist(RANDOM_TENANT));
        assertExitCodeCli(ExitCode.ALREADY_EXIST, "streaming create " + RANDOM_TENANT);
    }
    
    @Test
    @Order(3)
    void shouldListTenantsTest() {
        assertSuccessCli("streaming list");
        assertSuccessCli("streaming list -v");
        assertSuccessCli("streaming list --no-color");
        assertSuccessCli("streaming list -o json");
        assertSuccessCli("streaming list -o csv");
    }
    
    @Test
    @Order(4)
    void shouldListTenantsWithErrorsTest() {
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "streaming list -w");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "streaming list DB");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "streaming coaster");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "streaming list -o yaml");
    }
    
    @Test
    @Order(5)
    void shouldGetTenantTest() {
        assertSuccessCli("streaming get " + RANDOM_TENANT);
        assertSuccessCli("streaming get " + RANDOM_TENANT + " -o json");
        assertSuccessCli("streaming get " + RANDOM_TENANT + " -o csv");
        assertSuccessCli("streaming get " + RANDOM_TENANT + " --key status");
        assertSuccessCli("streaming get " + RANDOM_TENANT + " --key cloud");
        assertSuccessCli("streaming get " + RANDOM_TENANT + " --key pulsar_token");
        assertSuccessCli("streaming get " + RANDOM_TENANT + " --key region");
        assertSuccessCli("streaming describe " + RANDOM_TENANT);
        assertSuccessCli("streaming describe " + RANDOM_TENANT + " -o json");
        assertSuccessCli("streaming describe " + RANDOM_TENANT + " -o csv");
        assertSuccessCli("streaming describe " + RANDOM_TENANT + " --key status");
        assertSuccessCli("streaming describe " + RANDOM_TENANT + " --key cloud");
        assertSuccessCli("streaming describe " + RANDOM_TENANT + " --key pulsar_token");
        assertSuccessCli("streaming describe " + RANDOM_TENANT + " --key region");
        assertSuccessCli("streaming exist " + RANDOM_TENANT);
        assertSuccessCli("streaming status " + RANDOM_TENANT);
        assertSuccessCli("streaming pulsar-token " + RANDOM_TENANT);
    }
    
    @Test
    @Order(6)
    void shouldGetTenantWithErrorsTest() {
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "streaming invalid");
        assertExitCodeCli(ExitCode.NOT_FOUND, "streaming get " + RANDOM_TENANT + " --invalid");
        assertExitCodeCli(ExitCode.NOT_FOUND, "streaming get does-not-exist");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "streaming get " + RANDOM_TENANT + " -o yaml"); 
    }

    @Test
    @Order(7)
    void shouldCreateDotenvTest()  {
        assertSuccessCli("streaming create-dotenv %s -d %s".formatted(RANDOM_TENANT, "/tmp/"));
        Assertions.assertTrue(new File("/tmp/.env").exists());
    }

    @Test
    @Order(8)
    void shouldListCdcTest()  {
        assertSuccessCli("streaming create-dotenv %s -d %s".formatted(RANDOM_TENANT, "/tmp/"));
        Assertions.assertTrue(new File("/tmp/.env").exists());
    }

    @Test
    @Order(9)
    void shouldDeleteTenantTest() {
        // Given
        Assertions.assertTrue(ctx().getApiDevopsStreaming().exist(RANDOM_TENANT));
        // When
        assertSuccessCli("streaming delete " + RANDOM_TENANT);
        // Operation is "almost" instantaneous but little tempo to avoid failing test
        await("Tenant Deletion")
                .atMost(1, SECONDS)
                .until(() -> !ctx().getApiDevopsStreaming().exist(RANDOM_TENANT));
        // Then
        assertSuccessCli("streaming exist " + RANDOM_TENANT);
    }
    
    @Test
    @Order(9)
    void shouldDeleteTenantWithErrorsTest() {
        assertExitCodeCli(ExitCode.NOT_FOUND, "streaming delete does-not-exist");
    }

    @Test
    @Order(10)
    void testShouldListClouds() {
        assertSuccessCli("streaming list-clouds");
    }

    @AfterAll
    static void testShouldDeleteTemporaryTenant() {
        if (ctx().getApiDevopsStreaming().exist(RANDOM_TENANT)) {
            assertSuccessCli("streaming delete " + RANDOM_TENANT);
        }
    }

}
