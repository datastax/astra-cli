package com.datastax.astra.cli.test;

import com.datastax.astra.cli.core.exception.CannotStartProcessException;
import com.datastax.astra.cli.core.exception.FileSystemException;
import com.datastax.astra.cli.db.exception.DatabaseNameNotUniqueException;
import org.junit.jupiter.api.Test;

/**
 * Tests
 */
public class ExceptionTests extends AbstractCmdTest {

    @Test
    public void should_show_banner() {
        new CannotStartProcessException("test", new IllegalArgumentException());
        new DatabaseNameNotUniqueException("test");
        new FileSystemException("test");

    }
}
