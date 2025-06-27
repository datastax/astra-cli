package com.dtsx.astra.cli.core.exceptions;

import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import lombok.experimental.UtilityClass;
import lombok.val;
import picocli.CommandLine;

@UtilityClass
public class ExceptionHandlerUtils {
    public int handleAstraCliException(AstraCliException err, CommandLine cmd) {
        val formatted = AstraConsole.format(err.getMessage());

        if (formatted.stripTrailing().endsWith("\n")) {
            AstraConsole.getErr().print(formatted);
        } else {
            AstraConsole.getErr().println(formatted);
        }

        AstraLogger.exception(err);

        if (err.shouldDumpLogs()) {
            AstraLogger.dumpLogs();
        }

        if (err.shouldPrintHelpMessage()) {
            cmd.usage(cmd.getErr(), cmd.getColorScheme());
        }

        return 2;
    }
}
