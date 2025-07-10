package com.dtsx.astra.cli.core.exceptions.internal.cli;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraLogger;
import org.jetbrains.annotations.Nullable;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

public class CongratsYouFoundABugException extends AstraCliException {
    public CongratsYouFoundABugException(String error) {
        this(error, null);
    }

    public CongratsYouFoundABugException(String error, @Nullable Throwable cause) {
        super("""
          @|bold,red Error: "%s"|@
        
          @|bold Congratulations, you have found a bug in the Astra CLI.|@

          Please file an issue here: %s
  
          A full debug log was generated at %s
   
          If you can, please provide the following information:
            - The command you executed
            - The version of the CLI you are using
            - Your operating system and shell
            - Any other relevant information that can help us reproduce the issue
       
          Thank you for your help in making Astra CLI better!
        """.formatted(
            error,
            highlight("https://github.com/datastax/astra-cli/issues/new?template=bug_report.md"),
            highlight(AstraLogger.useSessionLogFilePath())
        ));

        if (cause != null) {
            AstraLogger.exception(cause);
        }
    }

    @Override
    public boolean shouldDumpLogs() {
        return true;
    }
}
