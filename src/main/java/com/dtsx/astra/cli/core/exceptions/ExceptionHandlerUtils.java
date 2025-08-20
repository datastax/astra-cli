package com.dtsx.astra.cli.core.exceptions;

import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import lombok.experimental.UtilityClass;
import lombok.val;
import picocli.CommandLine;

@UtilityClass
public class ExceptionHandlerUtils {
    public int handleAstraCliException(AstraCliException err, CommandLine cmd) {
        val response = OutputAll.response(err.getMessage(), err.getMetadata(), err.getNextSteps());

        val message = switch (OutputType.requested()) {
            case HUMAN -> response.renderAsHuman();
            case JSON -> response.renderAsJson();
            case CSV -> response.renderAsCsv();
        };

        if (message.stripTrailing().endsWith("\n")) {
            AstraConsole.getErr().print(message);
        } else {
            AstraConsole.getErr().println(message);
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
