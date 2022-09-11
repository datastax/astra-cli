package com.datastax.astra.cli.streaming;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.core.BaseCmd;
import com.datastax.astra.cli.core.out.JsonOutput;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.core.out.ShellPrinter;
import com.datastax.astra.cli.core.out.ShellTable;
import com.datastax.astra.cli.streaming.exception.TenantAlreadyExistExcepion;
import com.datastax.astra.cli.streaming.exception.TenantNotFoundException;
import com.datastax.astra.cli.streaming.pulsarshell.PulsarShellOptions;
import com.datastax.astra.cli.streaming.pulsarshell.PulsarShellUtils;
import com.datastax.astra.sdk.streaming.StreamingClient;
import com.datastax.astra.sdk.streaming.domain.CreateTenant;
import com.datastax.astra.sdk.streaming.domain.Tenant;

/**
 * Utility class for command `streaming`
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class OperationsStreaming {

    /** Command constants. */
    public static final String STREAMING = "streaming";
    /** default cloud.*/
    public static final String DEFAULT_CLOUD_PROVIDER = "aws";
    /** default region. */
    public static final String DEFAULT_CLOUD_REGION = "useast2";
    /** default plan. */
    public static final String DEFAULT_CLOUD_TENANT = "free";
    /** default email. */
    public static final String DEFAULT_EMAIL = "astra-cli@datastax.com";
    
    /** columns. */
    public static final String COLUMN_NAME   = "name";
    /** columns. */
    public static final String COLUMN_CLOUD  = "cloud";
    /** columns. */
    public static final String COLUMN_REGION = "region";
    /** columns. */
    public static final String COLUMN_STATUS = "Status";
    
    /**
     * List Tenants.
     * 
     * @return
     *      returned code
     */
    public static ExitCode listTenants() {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_NAME,    20);
        sht.addColumn(COLUMN_CLOUD,   10);
        sht.addColumn(COLUMN_REGION,  15);
        sht.addColumn(COLUMN_STATUS,  15);
        ShellContext.getInstance()
           .getApiDevopsStreaming()
           .tenants()
           .forEach(tnt -> {
                Map <String, String> rf = new HashMap<>();
                rf.put(COLUMN_NAME,   tnt.getTenantName());
                rf.put(COLUMN_CLOUD,  tnt.getCloudProvider());
                rf.put(COLUMN_REGION, tnt.getCloudRegion());
                rf.put(COLUMN_STATUS, tnt.getStatus());
                sht.getCellValues().add(rf);
        });
        ShellPrinter.printShellTable(sht);
        return ExitCode.SUCCESS;
    }
    
    /**
     * Show tenant details.
     *
     * @param tenantName
     *      tenant name
     * @throws TenantNotFoundException 
     *      error is tenant is not found
     * @return
     *      status code
     */
    public static ExitCode showTenant(String tenantName)
    throws TenantNotFoundException {
        Tenant tnt = getTenant(tenantName);
        ShellTable sht = ShellTable.propertyTable(15, 40);
        sht.addPropertyRow("Name", tnt.getTenantName());
        sht.addPropertyRow("Status", tnt.getStatus());
        sht.addPropertyRow("Cloud Provider", tnt.getCloudProvider());
        sht.addPropertyRow("Cloud region", tnt.getCloudRegion());
        sht.addPropertyRow("ClusterName", tnt.getClusterName());
        sht.addPropertyRow("PulsarVersion", tnt.getPulsarVersion());
        sht.addPropertyRow("JvmVersion", tnt.getJvmVersion());
        sht.addPropertyRow("Plan", tnt.getPlan());
        sht.addPropertyRow("WebServiceUrl", tnt.getWebServiceUrl());
        sht.addPropertyRow("BrokerServiceUrl", tnt.getBrokerServiceUrl());
        sht.addPropertyRow("WebSocketUrl", tnt.getWebsocketUrl());
        switch(ShellContext.getInstance().getOutputFormat()) {
            case json:
                ShellPrinter.printJson(new JsonOutput(ExitCode.SUCCESS, 
                            STREAMING + " " + BaseCmd.GET + " " + tenantName, sht));
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
     * Create a streaming tenant.
     *
     * @param ct
     *      tenant creation request
     * @throws TenantAlreadyExistExcepion
     *      already exist exception 
     * @return
     *      returned code.
     */
    public static ExitCode createStreamingTenant(CreateTenant ct) 
    throws TenantAlreadyExistExcepion {
        StreamingClient streamingClient = ShellContext.getInstance().getApiDevopsStreaming();
        if (streamingClient.tenant(ct.getTenantName()).exist()) {
            throw new TenantAlreadyExistExcepion(ct.getTenantName());
        }
        streamingClient.createTenant(ct);
        ShellPrinter.outputSuccess("Tenant '" + ct.getTenantName() + "' has being created.");
        return ExitCode.SUCCESS;
    }
    
    /**
     * Get tenant informations.
     *
     * @param tenantName
     *      tenant name
     * @return
     *      tenant when exist
     * @throws TenantNotFoundException
     *      tenant has not been foind 
     */
    private static Tenant getTenant(String tenantName) 
    throws TenantNotFoundException {
        StreamingClient streamingClient = ShellContext.getInstance().getApiDevopsStreaming();
        Optional<Tenant> optTenant = streamingClient.tenant(tenantName).find();
        if (!optTenant.isPresent()) {
            throw new TenantNotFoundException(tenantName);
        }
        return optTenant.get();
    }
   
    /**
     * Provide path of the pulsar conf for a tenant.
     *
     * @param tenant
     *      current tenant.
     * @return
     *      configuration file
     */
    private static File getPulsarConfFile(Tenant tenant) {
        return new File(PulsarShellUtils.getConfigurationFolder() + 
                File.separator + "client" 
                + "-" + tenant.getCloudProvider() 
                + "-" + tenant.getCloudRegion()
                + "-" + tenant.getTenantName()
                + ".conf");
    }
    
    /**
     * Create Pulsar conf for a tenant if needed.
     * 
     * @param tenant
     *      current tenant
     */
    public static void createPulsarConf(Tenant tenant) {
        File pulsarConf = getPulsarConfFile(tenant);
        if (pulsarConf.exists()) {
            LoggerShell.debug("Pulsar client.conf already exist");
        }
        PulsarShellUtils.generateConf(
                tenant.getCloudProvider() , 
                tenant.getCloudRegion(), 
                tenant.getPulsarToken(),
                pulsarConf.getAbsolutePath());
        LoggerShell.info("Pulsar client.conf has been generated.");
    }
    
    /**
     * Start Pulsar shell as a Process.
     * 
     * @param options
     *      options from the comman dline
     * @param tenantName
     *      current tenant name
     * @return
     *      error code
     * @throws TenantNotFoundException
     *      error if tenant is not found 
     */
    public static ExitCode startPulsarShell(PulsarShellOptions options, String tenantName) 
    throws TenantNotFoundException {
        
        // Retrieve tenant information from devops Apis or exception
        Tenant tenant = getTenant(tenantName);
        
        // Download and install pulsar-shell tarball when needed
        PulsarShellUtils.installPulsarShell();
        
        // Generating configuration file if needed (~/.astra/lunastreaming-shell-2.10.1.1/conf/...)
        createPulsarConf(tenant);
        
        try {
            System.out.println("Pulsar-shell is starting please wait for connection establishment...");
            Process cqlShProc = PulsarShellUtils.runPulsarShell(options, tenant, getPulsarConfFile(tenant));
            if (cqlShProc == null) {
                ExitCode.INTERNAL_ERROR.exit();
            }
            cqlShProc.waitFor();
        } catch (Exception e) {
            LoggerShell.error("Cannot start Pulsar Shel :" + e.getMessage());
            ExitCode.INTERNAL_ERROR.exit();
        }
        return ExitCode.SUCCESS;
    }
    
}
    