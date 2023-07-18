package com.dtsx.astra.cli.test;

import com.dtsx.astra.cli.core.exception.CannotStartProcessException;
import com.dtsx.astra.cli.core.exception.FileSystemException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests
 */
public class ExceptionTest extends AbstractCmdTest {

    @Test
    public void should_throw_common_exceptions() {
        Assertions.assertThrows(CannotStartProcessException.class, () -> {
            throw new CannotStartProcessException("test", null);
        });
        Assertions.assertThrows(FileSystemException.class, () -> {
            throw new FileSystemException("test");
        });

    }
}
