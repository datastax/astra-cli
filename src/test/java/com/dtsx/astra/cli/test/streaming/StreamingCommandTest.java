package com.dtsx.astra.cli.test.streaming;

import java.io.File;
import java.util.UUID;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;

import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.test.AbstractCmdTest;

/**
 * Testing Streaming commands
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@TestMethodOrder(OrderAnnotation.class)
public class StreamingCommandTest extends AbstractCmdTest {
    
    static String RANDOM_TENANT = "cli-" +
            UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);

    @BeforeAll
    public static void shouldCreateTemporaryTenantOnce() {
        try {
            assertSuccessCli("streaming create %s ".formatted(RANDOM_TENANT));
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        Assertions.assertTrue(ctx().getApiDevopsStreaming().tenant(RANDOM_TENANT).exist());
    }

    @Test
    @Order(1)
    public void shouldShowHelpTest() {
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
    public void shouldExistTenantTest() {
        // When
        assertSuccessCli("streaming exist " + RANDOM_TENANT);
        // Then
        Assertions.assertTrue(ctx().getApiDevopsStreaming().tenant(RANDOM_TENANT).exist());
        assertExitCodeCli(ExitCode.ALREADY_EXIST, "streaming create " + RANDOM_TENANT);
    }
    
    @Test
    @Order(3)
    public void shouldListTenantsTest() {
        assertSuccessCli("streaming list");
        //assertSuccessCli("streaming list -v");
        assertSuccessCli("streaming list --no-color");
        //assertSuccessCli("streaming list -o json");
        //assertSuccessCli("streaming list -o csv");
    }
    
    @Test
    @Order(4)
    public void shouldListTenantsWithErrorsTest() {
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "streaming list -w");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "streaming list DB");
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "streaming coaster");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "streaming list -o yaml");
    }
    
    @Test
    @Order(5)
    public void shouldGetTenantTest() {
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
    public void shouldGetTenantWithErrorsTest() {
        assertExitCodeCli(ExitCode.INVALID_ARGUMENT, "streaming invalid");
        assertExitCodeCli(ExitCode.NOT_FOUND, "streaming get " + RANDOM_TENANT + " --invalid");
        assertExitCodeCli(ExitCode.NOT_FOUND, "streaming get does-not-exist");
        assertExitCodeCli(ExitCode.INVALID_OPTION_VALUE, "streaming get " + RANDOM_TENANT + " -o yaml"); 
    }

    @Test
    @Order(7)
    public void shouldCreateDotenvTest()  {
        assertSuccessCli("streaming create-dotenv %s -d %s".formatted(RANDOM_TENANT, "/tmp/"));
        Assertions.assertTrue(new File("/tmp/.env").exists());
    }

    @Test
    @Order(8)
    public void shouldListCdcTest()  {
        assertSuccessCli("streaming create-dotenv %s -d %s".formatted(RANDOM_TENANT, "/tmp/"));
        Assertions.assertTrue(new File("/tmp/.env").exists());
    }

    @Test
    @Order(9)
    public void shouldDeleteTenantTest() throws InterruptedException {
        // Given
        Assertions.assertTrue(ctx().getApiDevopsStreaming().tenant(RANDOM_TENANT).exist());
        // When
        assertSuccessCli("streaming delete " + RANDOM_TENANT);
        // Operation is "almost" instantaneous but little tempo to avoid failing test
        Thread.sleep(500);
        // Then
        assertSuccessCli("streaming exist " + RANDOM_TENANT);
        Assertions.assertFalse(ctx().getApiDevopsStreaming().tenant(RANDOM_TENANT).exist());
    }
    
    @Test
    @Order(9)
    public void shouldDeleteTenantWithErrorsTest() {
        assertExitCodeCli(ExitCode.NOT_FOUND, "streaming delete does-not-exist");
    }

    @AfterAll
    public static void testShouldDeleteTemporaryTenant() {
        if (ctx().getApiDevopsStreaming().tenant(RANDOM_TENANT).exist()) {
            assertSuccessCli("streaming delete " + RANDOM_TENANT);
        }
    }

}
