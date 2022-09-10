package com.datastax.astra.cli.cmd.streaming;

import static com.datastax.astra.cli.cmd.streaming.OperationsStreaming.DEFAULT_CLOUD_PROVIDER;
import static com.datastax.astra.cli.cmd.streaming.OperationsStreaming.DEFAULT_CLOUD_REGION;
import static com.datastax.astra.cli.cmd.streaming.OperationsStreaming.DEFAULT_CLOUD_TENANT;
import static com.datastax.astra.cli.cmd.streaming.OperationsStreaming.DEFAULT_EMAIL;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.cmd.AbstractCmd;
import com.datastax.astra.cli.cmd.BaseCmd;
import com.datastax.astra.cli.exception.DatabaseNameNotUniqueException;
import com.datastax.astra.cli.exception.DatabaseNotFoundException;
import com.datastax.astra.cli.exception.ParamValidationException;
import com.datastax.astra.cli.exception.TenantAlreadyExistExcepion;
import com.datastax.astra.sdk.streaming.domain.CreateTenant;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;
/**
 * Will create a tenant when needed.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = AbstractCmd.CREATE, description = "Create a tenant in streaming with cli")
public class StreamingCreateCmd extends BaseCmd {
    /**
     * Database name or identifier
     */
    @Required
    @Arguments(title = "TENANT", description = "Tenant name (unique for the region)")
    protected String tenantName;
    
    // Create Tenant Options
    @Option(name = { "-c", "--cloud" }, description = "Cloud Provider to create a tenant")
    private String cloudProvider = DEFAULT_CLOUD_PROVIDER;
    
    /** option. */
    @Option(name = { "-r", "--region" }, description = "Cloud Region for the tenant")
    private String cloudRegion = DEFAULT_CLOUD_REGION;
    
    /** option. */
    @Option(name = { "-p", "--plan" }, description = "Plan for the tenant")    
    private String plan = DEFAULT_CLOUD_TENANT;
    
    /** option. */
    @Option(name = { "-e", "--email" }, description = "User Email")    
    private String email = DEFAULT_EMAIL;
    
    /** {@inheritDoc} */
    @Override
    public ExitCode execute() 
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException, ParamValidationException, TenantAlreadyExistExcepion {
        CreateTenant ct = new CreateTenant();
        ct.setCloudProvider(cloudProvider);
        ct.setCloudRegion(cloudRegion);
        ct.setPlan(plan);
        ct.setUserEmail(email);
        ct.setTenantName(tenantName);
        // TODO
        // Param Validations
        //throw new ParameterException(cloudProvider)
        // Does the tenant exist ?
        OperationsStreaming.createStreamingTenant(ct);
        return ExitCode.SUCCESS;
    }
    

}
