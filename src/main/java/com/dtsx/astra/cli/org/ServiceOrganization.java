package com.dtsx.astra.cli.org;

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

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.core.out.ShellTable;
import com.dtsx.astra.sdk.org.OrganizationsClient;
import com.dtsx.astra.sdk.org.domain.Organization;

import java.util.*;

/**
 * Operations on organizations
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class ServiceOrganization {
    
    /** cmd. */
    public static final String CMD_ID = "id";
    /** cmd. */
    public static final String CMD_NAME = "name";
     /** column names. */
    public static final String COLUMN_ID         = "id";
    /** column names. */
    public static final String COLUMN_NAME       = "Name";
    /** column names. */
    public static final String COLUMN_CLOUD      = "Cloud Provider";
    /** column names. */
    public static final String COLUMN_REGION_NAME = "Region";
    /** column names. */
    public static final String COLUMN_REGION_DISPLAY= "Full Name";

    /**
     * Singleton pattern.
     */
    private static ServiceOrganization instance;

    /**
     * Singleton pattern.
     *
     * @return
     *      organization.
     */
    public static synchronized ServiceOrganization getInstance() {
        if (instance == null) {
            instance = new ServiceOrganization();
        }
        return instance;
    }

    /**
     * Access Api devops from context.
     *
     * @return
     *      api devops
     */
    private OrganizationsClient apiDevopsOrg() {
        return CliContext.getInstance().getApiDevopsOrganizations();
    }

    /**
     * Hide default constructor.
     */
    private ServiceOrganization() {
    }

    /**
     * Return organization id.
     */
    public void getId() {

        AstraCliConsole.println(apiDevopsOrg().organizationId());
    }
    
    /**
     * Return organization name.
     */
    public void getName() {

        AstraCliConsole.println(apiDevopsOrg().organization().getName());
    }
    
    /**
     * Return organization info.
     */
    public void showOrg() {
        Organization org = apiDevopsOrg().organization();
        ShellTable sht = ShellTable.propertyTable(15, 40);
        sht.addPropertyRow(COLUMN_NAME, org.getName());
        sht.addPropertyRow(COLUMN_ID, org.getId());
        AstraCliConsole.printShellTable(sht);
    }

    /**
     * List streaming regions
     *
     * @param cloudProvider
     *      name of cloud provider
     * @param filter
     *      name of filter
     */
    public void listRegionsStreaming(String cloudProvider, String filter) {
        TreeMap<String, TreeMap<String, String>> sortedRegion = new TreeMap<>();
        CliContext.getInstance()
                  .getApiDevopsStreaming()
                  .serverlessRegions().forEach(r -> {
            String cloud = r.getCloudProvider().toLowerCase();
            sortedRegion.computeIfAbsent(cloud, k -> new TreeMap<>());
            sortedRegion.get(cloud).put(r.getName(), r.getDisplayName());
        });
        AstraCliConsole.printShellTable(buildShellTable(cloudProvider, filter, sortedRegion));
    }

    /**
     * Show organization database classic regions.
     *
     * @param cloudProvider
     *      name of cloud provider
     * @param filter
     *      name of filter
     */
    public void listRegionsDbClassic(String cloudProvider, String filter) {
        // Sorting Regions per cloud than region name
        TreeMap<String, TreeMap<String, String>> sortedRegion = new TreeMap<>();
        apiDevopsOrg().regions().forEach(r -> {
            String cloud = r.getCloudProvider().toString().toLowerCase();
            sortedRegion.computeIfAbsent(cloud, k -> new TreeMap<>());
            sortedRegion.get(cloud).put(r.getRegion(), r.getRegionDisplay());
        });
        // Building Table
        AstraCliConsole.printShellTable(buildShellTable(cloudProvider, filter, sortedRegion));
    }

    /**
     * Show serverless regions.
     *
     * @param cloudProvider
     *      name of cloud provider
     * @param filter
     *      name of filter
     */
    public void listRegionsDbServerless(String cloudProvider, String filter) {
        TreeMap<String, TreeMap<String, String>> sortedRegion = new TreeMap<>();
        apiDevopsOrg().regionsServerless().forEach(r -> {
            String cloud = r.getCloudProvider().toLowerCase();
            sortedRegion.computeIfAbsent(cloud, k -> new TreeMap<>());
            sortedRegion.get(cloud).put(r.getName(), r.getDisplayName());
        });
        AstraCliConsole.printShellTable(buildShellTable(cloudProvider, filter, sortedRegion));
    }

    /**
     * Common code to build Region List Table.
     *
     * @param cloudProvider
     *      filter on cloud provider name aws, azure,gcp
     * @param filter
     *      filter on others fields like region name or location
     * @param sortedRegion
     *      output of source call to get regions cloud,region,name
     * @return
     *      the table
     */
    private ShellTable buildShellTable(String cloudProvider, String filter, TreeMap<String, TreeMap<String, String>> sortedRegion) {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_CLOUD,          10);
        sht.addColumn(COLUMN_REGION_NAME,    20);
        sht.addColumn(COLUMN_REGION_DISPLAY, 30);
        sortedRegion.forEach((cloud, treemap) -> treemap.forEach((region, name) -> {
            Map<String, String> rf = new HashMap<>();
            if (cloudProvider == null || cloudProvider.equalsIgnoreCase(cloud)) {
                if (filter == null || region.contains(filter) || name.contains(filter)) {
                    rf.put(COLUMN_CLOUD, cloud);
                    rf.put(COLUMN_REGION_NAME, region);
                    rf.put(COLUMN_REGION_DISPLAY, name);
                    sht.getCellValues().add(rf);
                }
            }
        }));
        return sht;
    }

}
