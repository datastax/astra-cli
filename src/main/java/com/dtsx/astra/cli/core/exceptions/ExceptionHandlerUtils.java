package com.dtsx.astra.cli.core.exceptions;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import lombok.experimental.UtilityClass;
import lombok.val;
import picocli.CommandLine;

import static com.dtsx.astra.cli.core.output.ExitCode.UNCAUGHT;
import static com.dtsx.astra.cli.utils.StringUtils.withIndent;

@UtilityClass
public class ExceptionHandlerUtils {
    public int handleAstraCliException(AstraCliException err, CommandLine cmd, CliContext ctx) {
        val response = OutputAll.response(ctx.console().format(err.getMessage()), err.getMetadata(), err.getNextSteps(), err.getCode());

        val message = renderMessage(response, ctx);

        if (message.stripTrailing().endsWith("\n")) {
            ctx.console().error(message);
        } else {
            ctx.console().errorln(message);
        }

        ctx.log().exception(err);

        if (err.shouldDumpLogs()) {
            ctx.log().dumpLogsToFile();
        }

        if (err.shouldPrintHelpMessage()) {
            cmd.usage(cmd.getErr(), cmd.getColorScheme());
        }

        return 2;
    }

    public int handleUncaughtException(Throwable err, CliContext ctx) {
        val message = """
          @|bold,red An unexpected error occurred during the execution of the command:|@
        
        %s
        
          If necessary, file an issue here: @!https://github.com/datastax/astra-cli/issues/new?template=bug_report.md!@
  
          A full debug log was generated at @|underline @!%s!@|@
        """.formatted(
            withIndent(err.getMessage(), "  @!>!@ "),
            ctx.log().useSessionLogFilePath()
        );

        val rendered = renderMessage(OutputAll.response(message, null, null, UNCAUGHT), ctx);

        ctx.console().errorln(rendered);
        ctx.log().exception(err);
        ctx.log().dumpLogsToFile();

        return 99;
    }

    private String renderMessage(OutputAll response, CliContext ctx) {
        return switch (ctx.outputType()) {
            case HUMAN -> response.renderAsHuman(ctx);
            case JSON -> response.renderAsJson();
            case CSV -> response.renderAsCsv();
        };
    }
}
