package com.datastax.astra.cli.test;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;

import com.datastax.astra.cli.AstraCli;
import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.utils.AstraRcUtils;
import com.datastax.astra.sdk.config.AstraClientConfig;
import com.datastax.stargate.sdk.utils.Utils;

/**
 * Parent class for tests
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class AbstractCmdTest {
    
    /**
     * Syntaxic sugar to read environment variables.
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
        Optional<String> t2 = 
                readEnvVariable( AstraClientConfig.ASTRA_DB_APPLICATION_TOKEN);
        /** Env var is always first. */
        if (t2.isPresent()) { 
            return t2.get();
        }
        Optional<String> t1 = new AstraRcUtils().getSectionKey(
                AstraRcUtils.ASTRARC_DEFAULT, 
                AstraClientConfig.ASTRA_DB_APPLICATION_TOKEN);
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
    protected ShellContext ctx() {
        return ShellContext.getInstance();
    }
    
    /**
     * Help tests.
     * 
     * @return
     *      utils
     */
    protected AstraRcUtils astraRc() {
        return ctx().getAstraRc();
    }
    
    /**
     * Syntax sugar test.
     * @param cmd
     *      current command.
     */
    protected void assertOK(AbstractCmd cmd) {
        assertCode(ExitCode.SUCCESS, cmd);
    }
    
    /**
     * Test returned code.
     *
     * @param code
     *      returned code
     * @param cmd
     *      current command.
     */
    protected void assertCode(ExitCode code, AbstractCmd cmd) {
        Assertions.assertEquals(code, cmd.runCmd());
    }
    
    /**
     * Command line interface.
     * 
     * @return
     *      return codes
     */
    protected ExitCode astraCli(String cmd) {
        return AstraCli.runCli(AstraCli.class, cmd.split(" "));
    }
    
    
    /**
     * Command line interface.
     * 
     * @return
     *      return codes
     */
    protected ExitCode astraCli(String... cmd) {
        return AstraCli.runCli(AstraCli.class, cmd);
    }

}
