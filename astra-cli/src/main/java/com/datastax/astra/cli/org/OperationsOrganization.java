package com.datastax.astra.cli.org;

import java.util.HashMap;
import java.util.Map;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.out.JsonOutput;
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
     *
     * @return
     *      success
     */
    public static ExitCode getId() {
        System.out.print(orgClient().organizationId());
        return ExitCode.SUCCESS;
    }
    
    /**
     * Return organization name.
     *
     * @return
     *      success
     */
    public static ExitCode getName() {
        System.out.print(orgClient().organization().getName());
        return ExitCode.SUCCESS;
    }
    
    /**
     * Return organization infos.
     *
     * @return
     *      success
     */
    public static ExitCode showOrg() {
        Organization org = orgClient().organization();
        ShellTable sht = ShellTable.propertyTable(15, 40);
        sht.addPropertyRow(COLUMN_NAME, org.getName());
        sht.addPropertyRow(COLUMN_ID, org.getId());
        switch(ShellContext.getInstance().getOutputFormat()) {
            case json:
                ShellPrinter.printJson(new JsonOutput(ExitCode.SUCCESS, AbstractCmd.ORG , sht));
            break;
            case csv:
            case human:
            default:
                ShellPrinter.printShellTable(sht);
            break;
         }
        return ExitCode.SUCCESS;
    }
    
    /**
     * Show organization regions.
     *
     * @return
     *      status code
     */
    public static ExitCode listRegions() {
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
        return ExitCode.SUCCESS;
    }
         
    /**
     * Show serverless regions
     *     
     * @return
     *      status code
     */
    public static ExitCode listRegionsServerless() {
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
        return ExitCode.SUCCESS;
    }  

}
