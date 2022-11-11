package com.dtsx.astra.cli.streaming;

/*-
 * #%L
 * Astra Cli
 * %%
 * Copyright (C) 2022 DataStax
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import static com.dtsx.astra.cli.streaming.ServiceStreaming.DEFAULT_CLOUD_PROVIDER;
import static com.dtsx.astra.cli.streaming.ServiceStreaming.DEFAULT_CLOUD_REGION;
import static com.dtsx.astra.cli.streaming.ServiceStreaming.DEFAULT_CLOUD_TENANT;
import static com.dtsx.astra.cli.streaming.ServiceStreaming.DEFAULT_EMAIL;

import com.dtsx.astra.cli.core.AbstractConnectedCmd;
import com.dtsx.astra.sdk.streaming.domain.CreateTenant;
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
        // Param Validations
        //throw new ParameterException(cloudProvider)
        // Does the tenant exist ?
        ServiceStreaming.createStreamingTenant(ct);
    }
    

}
