package com.datastax.astra.cli.org;

import java.util.HashMap;
import java.util.Map;

import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.core.out.ShellPrinter;
import com.datastax.astra.cli.core.out.ShellTable;
import com.datastax.astra.sdk.organizations.OrganizationsClient;
import com.datastax.astra.sdk.organizations.domain.Organization;

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
     * Syntax sugar.
     * 
     * @return
     *      org client
     */
    private static final OrganizationsClient orgClient() {
        return ShellContext.getInstance().getApiDevopsOrganizations();
    }
    
    /**
     * Return organization id.
     */
    public static void getId() {
        System.out.print(orgClient().organizationId());
    }
    
    /**
     * Return organization name.
     */
    public static void getName() {
        System.out.print(orgClient().organization().getName());
    }
    
    /**
     * Return organization infos.
     */
    public static void showOrg() {
        Organization org = orgClient().organization();
        ShellTable sht = ShellTable.propertyTable(15, 40);
        sht.addPropertyRow(COLUMN_NAME, org.getName());
        sht.addPropertyRow(COLUMN_ID, org.getId());
        ShellPrinter.printShellTable(sht);
    }
    
    /**
     * Show organization regions.
     */
    public static void listRegions() {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_CLOUD,          10);
        sht.addColumn(COLUMN_REGION_NAME,    20);
        sht.addColumn(COLUMN_REGION_DISPLAY, 30);
        orgClient().regions()
           .forEach(r -> {
                Map <String, String> rf = new HashMap<>();
                rf.put(COLUMN_CLOUD,  r.getCloudProvider().toString());
                rf.put(COLUMN_REGION_NAME,  r.getRegion());
                rf.put(COLUMN_REGION_DISPLAY, r.getRegionDisplay());
                sht.getCellValues().add(rf);
        });
        ShellPrinter.printShellTable(sht);
    }
         
    /**
     * Show serverless regions
     */
    public static void listRegionsServerless() {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_CLOUD,          10);
        sht.addColumn(COLUMN_REGION_NAME,    20);
        sht.addColumn(COLUMN_REGION_DISPLAY, 30);
        orgClient().regionsServerless()
           .forEach(r -> {
                Map <String, String> rf = new HashMap<>();
                rf.put(COLUMN_CLOUD,  r.getCloudProvider().toString());
                rf.put(COLUMN_REGION_NAME,  r.getName());
                rf.put(COLUMN_REGION_DISPLAY, r.getDisplayName());
                sht.getCellValues().add(rf);
        });
        ShellPrinter.printShellTable(sht);
    }  

}
