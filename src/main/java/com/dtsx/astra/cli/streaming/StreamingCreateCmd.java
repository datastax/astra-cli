package com.dtsx.astra.cli.streaming;

/*-
 * #%L
 * Astra CLI
 * --
 * Copyright (C) 2022 - 2023 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.streaming.exception.TenantAlreadyExistException;
import com.dtsx.astra.sdk.streaming.domain.CreateTenant;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import org.apache.commons.lang3.StringUtils;

import static com.dtsx.astra.cli.streaming.ServiceStreaming.*;

/**
 * Will create a tenant when needed.
 */
@Command(name = "create", description = "Create a tenant in streaming with cli")
public class StreamingCreateCmd extends AbstractStreamingCmd {

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
     * Dedicated Cluster (cloud and region would be ignored)
     */
    @Option(name = { "-cl", "--cluster" }, description = "Dedicated cluster, replacement for cloud/region")
    String cluster;

    /**
     * Define proper plan
     */
    @Option(name = { "-p", "--plan" }, description = "Plan for the tenant")    
    String plan = DEFAULT_CLOUD_TENANT;
    
    /** option. */
    @Option(name = { "-e", "--email" }, description = "User Email")    
    String email = DEFAULT_EMAIL;

    /**
     * Create a tenant if it does not exist already
     **/
    @Option(name = { "--if-not-exist", "--if-not-exists" },
            description = "will create a new DB only if none with same name")
    protected boolean ifNotExist = false;

    /** {@inheritDoc} */
    @Override
    public void execute() {
        CreateTenant ct = new CreateTenant();
        if (!StringUtils.isEmpty(cluster)) {
            LoggerShell.info("Using dedicated cluster '%s' (cloud and region will be ignored)".formatted(cluster));
            ct.setClusterName(cluster);
        } else {
            ct.setCloudProvider(cloudProvider);
            ct.setCloudRegion(cloudRegion);
        }
        ct.setPlan(plan);
        ct.setUserEmail(email);
        ct.setTenantName(getTenant());
        ServiceStreaming.getInstance().createStreamingTenant(ct, ifNotExist);
    }
    

}
