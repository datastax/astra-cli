package com.datastax.astra.cli.streaming;

import static com.datastax.astra.cli.streaming.OperationsStreaming.DEFAULT_CLOUD_PROVIDER;
import static com.datastax.astra.cli.streaming.OperationsStreaming.DEFAULT_CLOUD_REGION;
import static com.datastax.astra.cli.streaming.OperationsStreaming.DEFAULT_CLOUD_TENANT;
import static com.datastax.astra.cli.streaming.OperationsStreaming.DEFAULT_EMAIL;

import com.datastax.astra.cli.core.AbstractConnectedCmd;
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
@Command(name = "create", description = "Create a tenant in streaming with cli")
public class StreamingCreateCmd extends AbstractConnectedCmd {

    /**
     * Tenant Name
     */
    @Required
    @Arguments(title = "TENANT", description = "Tenant name (unique for the region)")
    String tenantName;

    /**
     * Cloud provider or the tenant
     */
    @Option(name = { "-c", "--cloud" }, description = "Cloud Provider to create a tenant")
    String cloudProvider = DEFAULT_CLOUD_PROVIDER;

    /**
     * Cloud region or the tenant
     */
    @Option(name = { "-r", "--region" }, description = "Cloud Region for the tenant")
    String cloudRegion = DEFAULT_CLOUD_REGION;

    /**
     * Define proper plan
     */
    @Option(name = { "-p", "--plan" }, description = "Plan for the tenant")    
    String plan = DEFAULT_CLOUD_TENANT;
    
    /** option. */
    @Option(name = { "-e", "--email" }, description = "User Email")    
    String email = DEFAULT_EMAIL;
    
    /** {@inheritDoc} */
    @Override
    public void execute() {
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
    }
    

}
