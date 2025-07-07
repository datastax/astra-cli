package com.dtsx.astra.cli.core.exceptions.external;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.ExecutionExceptionHandler.ExternalExceptionMapper;
import com.dtsx.astra.sdk.exception.AuthenticationException;
import lombok.val;
import picocli.CommandLine;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.INVALID_TOKEN;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

public class AuthenticationExceptionMapper implements ExternalExceptionMapper<AuthenticationException> {
    @Override
    public Class<AuthenticationException> getExceptionClass() {
        return AuthenticationException.class;
    }

    @Override
    public AstraCliException mapExceptionInternal(AuthenticationException ex, CommandLine commandLine, CommandLine.ParseResult fullParseResult) {
        val userObj = commandLine.getCommandSpec().userObject();

        var profile = (userObj instanceof AbstractConnectedCmd<?> cmd)
            ? cmd.profile()
            : null;

        var tokenMsg =
            (profile != null && profile.isArgsProvided())
                ? "token provided via the command line" :
            (profile != null)
                ? "token provided in the configuration file for profile '" + highlight(profile.nameOrDefault()) + "'"
                : "used token";

        return new AstraCliException(INVALID_TOKEN, """
          @|bold,red Error: Invalid authentication token|@
        
          The %s is not valid or has expired.
        
          Cause: %s
        
          If you are using a non-production environment, make ensure that the correct environment is set in the profile or via the @!--env|@ command-line option.
        """.formatted(
            tokenMsg,
            ex.getMessage()
        ));
    }
}
