package com.dtsx.astra.cli.test;

import java.util.Optional;

import com.dtsx.astra.cli.config.AstraConfiguration;
import com.dtsx.astra.sdk.utils.AstraRc;
import com.dtsx.astra.sdk.utils.Utils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.ExitCode;

/**
 * Parent class for tests
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class AbstractCmdTest {

    /** Use to disable usage of CqlSh, Dsbulk and other during test for CI/CD. */
    public final static String FLAG_TOOLS = "disable_tools";

    public static String DB_TEST = "astra_cli_test";

    /** flag coding for tool disabling. */
    public static boolean disableTools = false;

    @BeforeAll
    public static void init() {
        readEnvVariable(FLAG_TOOLS).ifPresent(flag -> disableTools = Boolean.parseBoolean(flag));
        runCli("--version");
    }
    
    /**
     * Syntax sugar to read environment variables.
     *
     * @param key
     *      environment variable
     * @return
     *      if the value is there
     */
    public static Optional<String> readEnvVariable(String key) {
        if (Utils.hasLength(System.getProperty(key))) {
            return Optional.ofNullable(System.getProperty(key));
        } else if (Utils.hasLength(System.getenv(key))) {
            return Optional.ofNullable(System.getenv(key));
        }
        return Optional.empty();
    }
    
    /**
     * Astra Client to get a token
     */
    protected String getToken() {
        Optional<String> t2 = readEnvVariable(AstraRc.ASTRA_DB_APPLICATION_TOKEN);
        if (t2.isPresent()) {
            return t2.get();
        }
        Optional<String> t1 = new AstraConfiguration().getSectionKey(
                AstraConfiguration.ASTRARC_DEFAULT, 
                AstraRc.ASTRA_DB_APPLICATION_TOKEN);
        if (t1.isPresent()) { 
            return t1.get();
        }
        throw new IllegalStateException("Cannot find token");
    }
    
    /**
     * Help tests.
     * 
     * @return
     *      utils
     */
    public static CliContext ctx() {
        return CliContext.getInstance();
    }
   
    protected AstraConfiguration config() {
        return ctx().getConfiguration();
    }
    
    protected static ExitCode runCli(String cmd) {
        return runCli(cmd.split(" "));
    }
    
    protected static ExitCode runCli(String[] cmd) {
        return AstraCli.run(AstraCli.class, cmd);
    }
    
    protected static void assertSuccessCli(String cmd) {
        assertExitCodeCli(ExitCode.SUCCESS, cmd);
    }
    
    protected static void assertSuccessCli(String... cmd) {
        assertExitCodeCli(ExitCode.SUCCESS, cmd);
    }
    
    protected static void assertExitCodeCli(ExitCode code, String cmd) {
        Assertions.assertEquals(code, runCli(cmd));
    }
    
    protected static void assertExitCodeCli(ExitCode code, String[] cmd) {
        Assertions.assertEquals(code, runCli(cmd));
    }

    protected static void assertSuccessCql(String db, String query) {
        assertSuccessCli("db", "cqlsh",db, "-e", query);
    }

}
