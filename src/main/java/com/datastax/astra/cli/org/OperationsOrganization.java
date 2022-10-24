package com.datastax.astra.cli.org;

import com.datastax.astra.cli.core.CliContext;
import com.datastax.astra.cli.core.out.AstraCliConsole;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.core.out.ShellTable;
import com.datastax.astra.sdk.organizations.domain.Organization;

import java.util.HashMap;
import java.util.Map;

/**
 * Operations on organizations
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class OperationsOrganization {
    
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
    
    /**
     * Hide default constructor.
     */
    private OperationsOrganization() {}

    /**
     * Return organization id.
     */
    public static void getId() {
        LoggerShell.println(CliContext.getInstance().getApiDevopsOrganizations().organizationId());
    }
    
    /**
     * Return organization name.
     */
    public static void getName() {
        LoggerShell.println(CliContext.getInstance().getApiDevopsOrganizations().organization().getName());
    }
    
    /**
     * Return organization info.
     */
    public static void showOrg() {
        Organization org = CliContext.getInstance().getApiDevopsOrganizations().organization();
        ShellTable sht = ShellTable.propertyTable(15, 40);
        sht.addPropertyRow(COLUMN_NAME, org.getName());
        sht.addPropertyRow(COLUMN_ID, org.getId());
        AstraCliConsole.printShellTable(sht);
    }
    
    /**
     * Show organization regions.
     */
    public static void listRegions() {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_CLOUD,          10);
        sht.addColumn(COLUMN_REGION_NAME,    20);
        sht.addColumn(COLUMN_REGION_DISPLAY, 30);
        CliContext.getInstance().getApiDevopsOrganizations().regions()
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
    public static void listRegionsServerless() {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_CLOUD,          10);
        sht.addColumn(COLUMN_REGION_NAME,    20);
        sht.addColumn(COLUMN_REGION_DISPLAY, 30);
        CliContext.getInstance()
                .getApiDevopsOrganizations()
                .regionsServerless()
                .forEach(r -> {
                Map <String, String> rf = new HashMap<>();
                rf.put(COLUMN_CLOUD,  r.getCloudProvider());
                rf.put(COLUMN_REGION_NAME,  r.getName());
                rf.put(COLUMN_REGION_DISPLAY, r.getDisplayName());
                sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
    }  

}
