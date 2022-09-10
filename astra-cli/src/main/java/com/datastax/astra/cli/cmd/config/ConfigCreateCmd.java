package com.datastax.astra.cli.cmd.config;

import static com.datastax.astra.cli.ExitCode.CANNOT_CONNECT;
import static com.datastax.astra.cli.ExitCode.INVALID_PARAMETER;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.out.ShellPrinter;
import com.datastax.astra.sdk.organizations.OrganizationsClient;
import com.datastax.astra.sdk.organizations.domain.Organization;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Create a new section in configuration.
 * 
 * "astra config create"
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "create", description = "Create a new section in configuration")
public class ConfigCreateCmd extends BaseConfigCommand implements Runnable {
    
    /**
     * Section in configuration file to as as default.
     */
    @Required
    @Arguments(title = "section", description = "Section in configuration file to as as default.")
    protected String sectionName;
   
    /** Authentication token used if not provided in config. */
    @Option(name = { "-t", "--token" }, title = "AuthToken", description = "Key to use authenticate each call.")
    protected String token;
    
    /** {@inheritDoc} */
    @Override
    public void run() {
        if (token == null) {
            ShellPrinter.outputError(ExitCode.INVALID_PARAMETER, "Please Provide a token with option -t, --token");
            ExitCode.INVALID_PARAMETER.exit();
        }
        if (!token.startsWith("AstraCS:")) {
            ShellPrinter.outputError(ExitCode.INVALID_PARAMETER, "Your token should start with 'AstraCS:'");
            ExitCode.INVALID_PARAMETER.exit();
        }
        
        try {
            OrganizationsClient apiOrg  = new OrganizationsClient(token);
            Organization o = apiOrg.organization();
            if (sectionName == null) {
                sectionName = o.getName();
            }
            getAstraRc().createSectionWithToken(sectionName, token);
            getAstraRc().save();
            ShellPrinter.outputSuccess("Configuration Saved.\n");
        } catch(Exception e) {
            ShellPrinter.outputError(CANNOT_CONNECT, "Token provided is invalid. It was not possible to connect to Astra.");
            INVALID_PARAMETER.exit();
        }
    }
}
