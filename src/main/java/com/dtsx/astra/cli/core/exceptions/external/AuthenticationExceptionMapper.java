package com.dtsx.astra.cli.core.exceptions.external;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.ExecutionExceptionHandler.ExternalExceptionMapper;
import com.dtsx.astra.sdk.exception.AuthenticationException;
import lombok.val;
import picocli.CommandLine;

import static com.dtsx.astra.cli.core.output.ExitCode.INVALID_TOKEN;

public class AuthenticationExceptionMapper implements ExternalExceptionMapper<AuthenticationException> {
    @Override
    public boolean canMap(Exception ex) {
        return ex instanceof AuthenticationException;
    }

    @Override
    public AstraCliException mapExceptionInternal(AuthenticationException ex, CommandLine commandLine, CommandLine.ParseResult fullParseResult, CliContext ctx) {
        val userObj = commandLine.getCommandSpec().userObject();

        val profile = (userObj instanceof AbstractConnectedCmd<?> cmd)
            ? cmd.profile()
            : null;

        val tokenMsg =
            (profile != null && profile.isReconstructedFromCreds())
                ? "token provided via the command line" :
            (profile != null)
                ? "token provided in the configuration file for profile '" + ctx.highlight(profile.nameOrDefault()) + "'"
                : "used token";

        return new AstraCliException(INVALID_TOKEN, """
          @|bold,red Error: Invalid authentication token|@
        
          The %s is not valid or has expired.
        
          Cause: %s
        
          If you are using a non-production environment, ensure that the correct environment is set in the profile or via the @'!--env!@ command-line option.
        """.formatted(
            tokenMsg,
            ex.getMessage()
        ));
    }
}
