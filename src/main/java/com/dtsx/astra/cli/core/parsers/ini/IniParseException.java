package com.dtsx.astra.cli.core.parsers.ini;

import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

public class IniParseException extends Exception {
    public IniParseException(String message, int lineNumber, String line) {
        super(trimIndent("""
          Failed to parse the configuration file at line @'!%s!@:

          "%s"

          Problematic line:
              %s

          Please check the syntax near the reported line.
        """.formatted(
            lineNumber,
            message,
            line
        )));
    }
}
