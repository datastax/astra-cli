package com.dtsx.astra.cli.core.exceptions.internal.cli;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.ExitCode;
import com.dtsx.astra.cli.utils.MiscUtils;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import static com.dtsx.astra.cli.utils.StringUtils.NL;
import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;

public class CongratsYouFoundABugException extends AstraCliException {
    public CongratsYouFoundABugException(String error) {
        this(error, null);
    }

    public CongratsYouFoundABugException(String msg, @Nullable Throwable cause) {
        super(ExitCode.BUG, mkErrorMsg(msg, cause));

        if (cause != null && AstraCli.unsafeGlobalCliContext() != null) {
            AstraCli.unsafeGlobalCliContext().get().log().exception(cause);
        }
    }

    private static String mkErrorMsg(String msg, @Nullable Throwable cause) {
        val ctx = AstraCli.unsafeGlobalCliContext();

        val debugLogMsg = (ctx != null)
            ? "A full debug log was generated at @!" + ctx.get().log().useSessionLogFilePath() + "!@"
            : "A debug log was not able to be generated." + (cause != null ? " The stacktrace will be appended to this message instead." : "");

        val stacktraceMsg = (ctx == null && cause != null)
            ? NL + NL + MiscUtils.captureStackTrace(cause)
            : "";

        return trimIndent("""
          @|bold,red Error: "%s"|@
        
          @|bold Congratulations, you have found a bug in the Astra CLI.|@

          Please file an issue here: @!https://github.com/datastax/astra-cli/issues/new?template=bug_report.md!@
  
          %s
   
          If you can, please provide the following information:
            - The command you executed
            - The version of the CLI you are using
            - Your operating system and shell
            - Any other relevant information that can help us reproduce the issue
       
          Thank you for your help in making Astra CLI better!%s
        """).formatted(
            msg,
            debugLogMsg,
            stacktraceMsg
        );
    }

    @Override
    public boolean shouldDumpLogs() {
        return true;
    }
}
