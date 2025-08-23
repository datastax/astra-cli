package com.dtsx.astra.cli.core.exceptions;

import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import lombok.experimental.UtilityClass;
import lombok.val;
import picocli.CommandLine;

import static com.dtsx.astra.cli.core.output.ExitCode.UNCAUGHT;
import static com.dtsx.astra.cli.utils.StringUtils.withIndent;

@UtilityClass
public class ExceptionHandlerUtils {
    public int handleAstraCliException(AstraCliException err, CommandLine cmd) {
        val response = OutputAll.response(err.getMessage(), err.getMetadata(), err.getNextSteps(), err.getCode());

        val message = renderMessage(response);

        if (message.stripTrailing().endsWith("\n")) {
            AstraConsole.error(message);
        } else {
            AstraConsole.errorln(message);
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

    public int handleUncaughtException(Throwable err) {
        val message = """
          @|bold,red An unexpected error occurred during the execution of the command:|@
        
        %s
        
          If necessary, file an issue here: @!https://github.com/datastax/astra-cli/issues/new?template=bug_report.md!@
  
          A full debug log was generated at @|underline @!%s!@|@
        """.formatted(
            withIndent(err.getMessage(), "  @!>!@ "),
            AstraLogger.useSessionLogFilePath()
        );

        val rendered = renderMessage(OutputAll.response(message, null, null, UNCAUGHT));

        AstraConsole.errorln(rendered);
        AstraLogger.exception(err);
        AstraLogger.dumpLogs();

        return 99;
    }

    private String renderMessage(OutputAll response) {
        return switch (OutputType.requested()) {
            case HUMAN -> response.renderAsHuman();
            case JSON -> response.renderAsJson();
            case CSV -> response.renderAsCsv();
        };
    }
}
