package com.datastax.astra.cli.org;

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

import com.datastax.astra.cli.core.CliContext;
import com.datastax.astra.cli.core.out.AstraCliConsole;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.core.out.ShellTable;
import com.datastax.astra.sdk.organizations.OrganizationsClient;
import com.datastax.astra.sdk.organizations.domain.Organization;

import java.util.HashMap;
import java.util.Map;

/**
 * Operations on organizations
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class OrganizationService {
    
    /** cmd. */
    public static final String CMD_ID = "id";
    /** cmd. */
    public static final String CMD_NAME = "name";
    /** cmd. */
    public static final String CMD_REGIONS = "list-regions-classic";
    /** cmd. */
    public static final String CMD_SERVERLESS = "list-regions-serverless";
    
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

    /** Http Client for devops Api. */
    private final OrganizationsClient orgClient;

    /**
     * Singleton pattern.
     */
    private static OrganizationService instance;

    /**
     * Singleton pattern.
     *
     * @return
     *      organization.
     */
    public static synchronized OrganizationService getInstance() {
        if (instance == null) {
            instance = new OrganizationService(CliContext.getInstance().getApiDevopsOrganizations());
        }
        return instance;
    }

    /**
     * Hide default constructor.
     *
     * @param orgClient
     *      http client to organization
     */
    private OrganizationService(OrganizationsClient orgClient) {
        this.orgClient = orgClient;
    }

    /**
     * Return organization id.
     */
    public void getId() {
        LoggerShell.println(orgClient.organizationId());
    }
    
    /**
     * Return organization name.
     */
    public void getName() {
        LoggerShell.println(orgClient.organization().getName());
    }
    
    /**
     * Return organization info.
     */
    public void showOrg() {
        Organization org = orgClient.organization();
        ShellTable sht = ShellTable.propertyTable(15, 40);
        sht.addPropertyRow(COLUMN_NAME, org.getName());
        sht.addPropertyRow(COLUMN_ID, org.getId());
        AstraCliConsole.printShellTable(sht);
    }
    
    /**
     * Show organization regions.
     */
    public void listRegions() {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_CLOUD,          10);
        sht.addColumn(COLUMN_REGION_NAME,    20);
        sht.addColumn(COLUMN_REGION_DISPLAY, 30);
        orgClient.regions()
           .forEach(r -> {
                Map <String, String> rf = new HashMap<>();
                rf.put(COLUMN_CLOUD,  r.getCloudProvider().toString());
                rf.put(COLUMN_REGION_NAME,  r.getRegion());
                rf.put(COLUMN_REGION_DISPLAY, r.getRegionDisplay());
                sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
    }
         
    /**
     * Show serverless regions
     */
    public void listRegionsServerless() {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_CLOUD,          10);
        sht.addColumn(COLUMN_REGION_NAME,    20);
        sht.addColumn(COLUMN_REGION_DISPLAY, 30);
        orgClient.regionsServerless().forEach(r -> {
            Map <String, String> rf = new HashMap<>();
            rf.put(COLUMN_CLOUD,  r.getCloudProvider());
            rf.put(COLUMN_REGION_NAME,  r.getName());
            rf.put(COLUMN_REGION_DISPLAY, r.getDisplayName());
            sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
    }  

}
