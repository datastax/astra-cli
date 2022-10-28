package com.dtsx.astra.cli.test;

import com.dtsx.astra.cli.core.exception.CannotStartProcessException;
import com.dtsx.astra.cli.core.exception.FileSystemException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests
 */
public class ExceptionTests extends AbstractCmdTest {

    @Test
    public void should_throw_common_exceptions() {
        new CannotStartProcessException("test", new IllegalArgumentException());
        Assertions.assertThrows(FileSystemException.class, () -> {
            throw new FileSystemException("test");
        });

    }
}
