package com.datastax.astra.cli.config;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.core.exception.InvalidTokenException;
import com.datastax.astra.cli.core.exception.TokenNotFoundException;
import com.datastax.astra.cli.core.out.AstraCliConsole;
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
public class ConfigCreateCmd extends AbstractConfigCmd {
    
    /** Section in configuration file to as as default. */
    @Required
    @Arguments(title = "sectionName", description = "Section in configuration file to as as default.")
    protected String sectionName;
   
    /** Authentication token used if not provided in config. */
    @Option(name = { "-t", "--token" }, title = "AuthToken", description = "Key to use authenticate each call.")
    protected String token;
    
    /** {@inheritDoc} */
    @Override
    public void execute() 
    throws TokenNotFoundException, InvalidTokenException  {
        if (token == null) {
            AstraCliConsole.outputError(ExitCode.INVALID_PARAMETER, "Please Provide a token with option -t, --token");
            throw new TokenNotFoundException();
        }
        if (!token.startsWith("AstraCS:")) {
            AstraCliConsole.outputError(ExitCode.INVALID_PARAMETER, "Your token should start with 'AstraCS:'");
            throw new InvalidTokenException();
        }
        OrganizationsClient apiOrg  = new OrganizationsClient(token);
        Organization o = apiOrg.organization();
        if (sectionName == null) {
            sectionName = o.getName();
        }
        ctx().getAstraRc().createSectionWithToken(sectionName, token);
        ctx().getAstraRc().save();
        AstraCliConsole.outputSuccess("Configuration Saved.\n");
    }
    
}
