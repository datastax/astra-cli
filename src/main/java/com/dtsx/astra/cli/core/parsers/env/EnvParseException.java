package com.dtsx.astra.cli.core.parsers.env;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

public class EnvParseException extends Exception {
    public EnvParseException(String message, int lineNumber, String line) {
        super(trimIndent("""
          Failed to parse the .env file at line %s:

          "%s"

          Problematic line:
              %s

          Please check the syntax near the reported line.
        """.formatted(
            highlight(lineNumber),
            message,
            line
        )));
    }
}
