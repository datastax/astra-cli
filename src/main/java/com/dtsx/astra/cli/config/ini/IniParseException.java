package com.dtsx.astra.cli.config.ini;

import java.io.File;

public class IniParseException extends Exception {
    public IniParseException(File file, String message, int lineNumber, String line) {
        super("""
            Failed to parse the configuration file at line %d in '%s':
            
            "%s"
            
            Problematic line:
                %s
            
            Please check the syntax near the reported line."""
            .stripIndent().formatted(
                lineNumber,
                file.getAbsolutePath(),
                message,
                line
            ));
    }
}
