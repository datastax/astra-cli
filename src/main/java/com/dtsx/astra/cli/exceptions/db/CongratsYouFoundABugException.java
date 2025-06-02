package com.dtsx.astra.cli.exceptions.db;

import com.dtsx.astra.cli.exceptions.AstraCliException;
import com.dtsx.astra.cli.output.AstraColors;
import com.dtsx.astra.cli.output.AstraLogger;
import com.dtsx.astra.cli.output.ExitCode;

public class CongratsYouFoundABugException extends AstraCliException {
    public CongratsYouFoundABugException(String error) {
        super("""
            Congratulations, you have found a bug in the Astra CLI.
            
            ERROR: "%s"
            
            Please file an issue here: %s
            
            A full debug log was generated at %s
            
            If you can, please provide the following information:
              - The command you executed
              - The version of the CLI you are using
              - Your operating system and shell
              - Any other relevant information that can help us reproduce the issue
            
            Thank you for your help in making Astra CLI better!"""
            .stripIndent().formatted(
                error,
                AstraColors.BLUE_300.use("https://github.com/datastax/astra-cli/issues/new?template=bug_report.md"),
                AstraColors.BLUE_300.use(AstraLogger.getSessionLogFilePath())
            ));
    }

    @Override
    public boolean shouldDumpLogs() {
        return true;
    }

    @Override
    public ExitCode getExitCode() {
        return ExitCode.INTERNAL_ERROR;
    }
}
