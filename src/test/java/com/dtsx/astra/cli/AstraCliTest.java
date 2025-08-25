package com.dtsx.astra.cli;

import com.dtsx.astra.cli.core.CliEnvironment;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystems;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class AstraCliTest {

    @BeforeAll
    static void setup() {
        CliEnvironment.setFileSystem(FileSystems.getDefault());
    }

    @Test
    void testHelloWorld() {
        System.out.println("a");
    }

    @Test
    void testCliMainWithHelp() {
        System.out.println("Running AstraCli main with --help argument");

        assertDoesNotThrow(() -> {
            AstraCli.run("--help");
        });
    }
}
