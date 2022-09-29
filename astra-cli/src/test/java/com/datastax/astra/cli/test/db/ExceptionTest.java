package com.datastax.astra.cli.test.db;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.datastax.astra.cli.test.AbstractCmdTest;
import com.github.rvesse.airline.parser.errors.ParseArgumentsMissingException;
import com.github.rvesse.airline.parser.errors.ParseArgumentsUnexpectedException;
import com.github.rvesse.airline.parser.errors.ParseCommandUnrecognizedException;

/**
 * Tests DSBulk.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class ExceptionTest extends AbstractCmdTest {
    
    @Test
    public void should_fail_unregognized_command() {
        Assertions.assertThrows(
                ParseCommandUnrecognizedException.class,
                () -> runCli("db create2"));
    }
    
    @Test
    public void should_fail_argument_missing() {
        Assertions.assertThrows(
                ParseArgumentsMissingException.class,
                () -> runCli("db create"));
    }
    

    @Test
    public void should_fail_argument_count() {
        Assertions.assertThrows(
                ParseArgumentsUnexpectedException.class,
                () -> runCli("db create A B"));
    }
    
    
    
    
    
}
