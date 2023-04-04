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

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.core.exception.CannotStartProcessException;
import com.dtsx.astra.cli.core.exception.FileSystemException;
import com.dtsx.astra.cli.core.exception.InvalidCloudProviderException;
import com.dtsx.astra.cli.core.exception.InvalidRegionException;
import com.dtsx.astra.cli.core.out.*;
import com.dtsx.astra.cli.org.ServiceOrganization;
import com.dtsx.astra.cli.streaming.exception.TenantAlreadyExistException;
import com.dtsx.astra.cli.streaming.exception.TenantNotFoundException;
import com.dtsx.astra.cli.streaming.pulsarshell.PulsarShellOptions;
import com.dtsx.astra.cli.streaming.pulsarshell.PulsarShellUtils;
import com.dtsx.astra.cli.utils.EnvFile;
import com.dtsx.astra.sdk.streaming.AstraStreamingClient;
import com.dtsx.astra.sdk.streaming.TenantClient;
import com.dtsx.astra.sdk.streaming.domain.CreateTenant;
import com.dtsx.astra.sdk.streaming.domain.StreamingRegion;
import com.dtsx.astra.sdk.streaming.domain.Tenant;
import com.dtsx.astra.sdk.utils.Assert;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility class for command `streaming`.
 */
public class ServiceStreaming implements AstraColorScheme {

    /** Command constants. */
    public static final String CMD_STATUS    = "status";
    /** Command constants. */
    public static final String CMD_EXIST     = "exist";
    /** Command constants. */
    public static final String CMD_GET_TOKEN = "pulsar-token";
    
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
    public static final String COLUMN_STATUS = "status";
    /** columns. */
    public static final String COLUMN_CLUSTER = "cluster";
    /** columns. */
    public static final String COLUMN_NAMESPACE = "namespace";
    /** columns. */
    public static final String COLUMN_DB = "database";
    /** columns. */
    public static final String COLUMN_KEYSPACE = "keyspace";
    /** columns. */
    public static final String COLUMN_TABLE = "table";
    /** Working object. */
    static final String TENANT = "Tenant";

    /**
     * Singleton Pattern
     */
    private static ServiceStreaming instance;

    /**
     * Hide default constructor
     */
    private ServiceStreaming() {}

    /**
     * Singleton Pattern.
     *
     * @return
     *      instance of the service.
     */
    public static synchronized ServiceStreaming getInstance() {
        if (null == instance) {
            instance = new ServiceStreaming();
        }
        return instance;
    }

    /** limit resource usage by caching tenant clients. */
    private Map<String, TenantClient> cacheTenantClient = new HashMap<>();

    /**
     * Syntax Sugar to work with Streaming Devops Apis.
     * 
     * @return
     *      streaming tenant.
     */
    private AstraStreamingClient getApiDevopsStreaming() {
        return  CliContext.getInstance().getApiDevopsStreaming();
    }
    
    /**
     * Syntax sugar and caching for tenant clients.
     * 
     * @param tenantName
     *      current tenant name.
     * @return
     *      tenant client or error
     */
    private TenantClient tenantClient(String tenantName) {
        return cacheTenantClient.computeIfAbsent(tenantName, (t) -> getApiDevopsStreaming().tenant(t));
    }
    
    /**
     * Get tenant information.
     *
     * @param tenantName
     *      tenant name
     * @return
     *      tenant when exist
     * @throws TenantNotFoundException
     *      tenant has not been found
     */
    private Tenant getTenant(String tenantName)
    throws TenantNotFoundException {
        return getApiDevopsStreaming().get(tenantName);
    }

    /**
     * Validate that provided region is in the target cloud.
     *
     * @param cloud
     *      provided cloud
     * @param region
     *      provided region
     */
    public void validateCloudRegion(String cloud, String region) {
        Assert.hasLength(region, "region name");

        if (cloud == null && "".equals(cloud)) {
            TreeMap<String, TreeMap<String, String>> regions =
                    ServiceOrganization.getInstance().getStreamingRegions();
            if (!regions.containsKey(cloud.toLowerCase())) {
                // value provided in --cloud is invalid
                throw new InvalidCloudProviderException(cloud);
            } else if (((TreeMap<String, String>) regions.get(cloud.toLowerCase()))
                    .keySet().contains(region.toLowerCase())) {
                // cloud ok, but invalid region
                throw new InvalidRegionException(cloud, region);
            }
            // OK
        } else if (getApiDevopsStreaming()
                    .regions()
                    .findAllServerless()
                    .map(StreamingRegion::getName)
                    .filter(r -> r.equals(region.toLowerCase()))
                    .findFirst().isEmpty()) {
            throw new InvalidRegionException(region);
        }
    }

    /**
     * List Tenants.
     */
    public void listTenants() {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_NAME,    20);
        sht.addColumn(COLUMN_CLOUD,   10);
        sht.addColumn(COLUMN_REGION,  15);
        sht.addColumn(COLUMN_STATUS,  15);
        getApiDevopsStreaming()
           .findAll()
           .forEach(tnt -> {
                Map <String, String> rf = new HashMap<>();
                rf.put(COLUMN_NAME,   tnt.getTenantName());
                rf.put(COLUMN_CLOUD,  tnt.getCloudProvider());
                rf.put(COLUMN_REGION, tnt.getCloudRegion());
                if (!CliContext.getInstance().isNoColor()) {
                    rf.put(COLUMN_STATUS, StringBuilderAnsi.colored(tnt.getStatus(), getStatusColor(tnt.getStatus())));
                } else {
                    rf.put(COLUMN_STATUS, tnt.getStatus());
                }
                sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
    }

    /**
     * Utility to color the status based on the value.
     *
     * @param tenantStatus
     *      current tenant status
     * @return
     *      color for status
     */
    private AnsiColorRGB getStatusColor(String tenantStatus) {
        Assert.hasLength(tenantStatus, "Status");
        switch(tenantStatus.toLowerCase()) {
            case "active" : return green500;
            case "error"  : return red500;
            default: return neutral300;
        }
    }

    /**
     * List cdc for a tenant.
     *
     * @param tenantName
     *      tenant name
     * @throws TenantNotFoundException
     *      tenant has not been found
     */
    public void listCdc(String tenantName)
    throws TenantNotFoundException {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_CLUSTER,   15);
        sht.addColumn(COLUMN_NAMESPACE,  15);
        sht.addColumn(COLUMN_DB, 15);
        sht.addColumn(COLUMN_KEYSPACE,  15);
        sht.addColumn(COLUMN_TABLE,  15);
        sht.addColumn(COLUMN_STATUS,  15);
        tenantClient(tenantName)
                .cdc()
                .list()
                .forEach(cdc -> {
                    Map <String, String> rf = new HashMap<>();
                    rf.put(COLUMN_CLUSTER,   cdc.getClusterName());
                    rf.put(COLUMN_NAMESPACE,  cdc.getNamespace());
                    rf.put(COLUMN_DB, cdc.getDatabaseName());
                    rf.put(COLUMN_KEYSPACE, cdc.getKeyspace());
                    rf.put(COLUMN_TABLE, cdc.getDatabaseTable());
                    rf.put(COLUMN_STATUS, cdc.getConnectorStatus());
                    sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
    }
    
    /**
     * Show tenant details.
     *
     * @param tenantName
     *      tenant name
     * @param key
     *      display only one key
     * @throws TenantNotFoundException 
     *      error is tenant is not found
     */
    public void showTenant(String tenantName, StreamingGetCmd.StreamingGetKeys key)
    throws TenantNotFoundException {
        Tenant tnt = getTenant(tenantName);
        if (key == null) {
            ShellTable sht = ShellTable.propertyTable(15, 40);
            sht.addPropertyRow("Name", tnt.getTenantName());
            sht.addPropertyRow(COLUMN_STATUS, tnt.getStatus());
            sht.addPropertyRow("Cloud Provider", tnt.getCloudProvider());
            sht.addPropertyRow("Cloud region", tnt.getCloudRegion());
            sht.addPropertyRow("Cluster Name", tnt.getClusterName());
            sht.addPropertyRow("Pulsar Version", tnt.getPulsarVersion());
            sht.addPropertyRow("Jvm Version", tnt.getJvmVersion());
            sht.addPropertyRow("Plan", tnt.getPlan());
            sht.addPropertyRow("WebServiceUrl", tnt.getWebServiceUrl());
            sht.addPropertyRow("BrokerServiceUrl", tnt.getBrokerServiceUrl());
            sht.addPropertyRow("WebSocketUrl", tnt.getWebsocketUrl());
            if (CliContext.getInstance().getOutputFormat() == OutputFormat.JSON) {
                AstraCliConsole.printJson(new JsonOutput<>(ExitCode.SUCCESS,
                        STREAMING + " get " + tenantName, sht));
            } else {
                AstraCliConsole.printShellTable(sht);
            }
        }  else {
            switch (key) {
                case CLOUD -> AstraCliConsole.println(tnt.getCloudProvider());
                case PULSAR_TOKEN -> AstraCliConsole.println(tnt.getPulsarToken());
                case REGION -> AstraCliConsole.println(tnt.getCloudRegion());
                case STATUS -> AstraCliConsole.println(tnt.getStatus());
            }
        }
    }
    
    /**
     * Delete database if exists.
     * 
     * @param tenantName
     *      tenant name
     * @throws TenantNotFoundException 
     *      error if tenant name is not unique
     */
    public void deleteTenant(String tenantName)
    throws TenantNotFoundException {
        getApiDevopsStreaming().delete(tenantName);
        AstraCliConsole.outputSuccess("Deleting Tenant '" + tenantName + "'");
    }
    
    /**
     * Display status of a tenant.
     * 
     * @param tenantName
     *      tenant name
     * @throws TenantNotFoundException 
     *      error if tenant is not found
     */
    public void showTenantStatus(String tenantName)
    throws TenantNotFoundException {
        Tenant tnt = getTenant(tenantName);
        AstraCliConsole.outputSuccess("%s '%s' has status '%s'"
                .formatted(TENANT, tenantName, tnt.getStatus()));
    }
    
    /**
     * Display existence of a tenant.
     * 
     * @param tenantName
     *      tenant name
     */
    public void showTenantExistence(String tenantName) {
        if (getApiDevopsStreaming().exist(tenantName)) {
            AstraCliConsole.outputSuccess("%s '%s' exists.".formatted(TENANT, tenantName));
        } else {
            AstraCliConsole.outputSuccess("%s '%s' does not exist.".formatted(TENANT, tenantName));
        }
    }
    
    /**
     * Display token of a tenant.
     * 
     * @param tenantName
     *      database name
     * @throws TenantNotFoundException 
     *      error if tenant is not found
     */
    public void showTenantPulsarToken(String tenantName)
    throws TenantNotFoundException {
       AstraCliConsole.println(getTenant(tenantName).getPulsarToken());
    }
    
    /**
     * Create a streaming tenant.
     *
     * @param ct
     *      tenant creation request
     * @param ifNotExistFlag
     *      will try to create and raise error only if not present
     * @throws TenantAlreadyExistException
     *      already exist exception 
     */
    public void createStreamingTenant(CreateTenant ct, boolean ifNotExistFlag)
    throws TenantAlreadyExistException {
        validateCloudRegion(ct.getCloudProvider(), ct.getCloudRegion());
        boolean tenantExist = getApiDevopsStreaming().exist(ct.getTenantName());
        if (tenantExist && !ifNotExistFlag) {
            throw new TenantAlreadyExistException(ct.getTenantName());
        }
        if (!tenantExist) {
            getApiDevopsStreaming().create(ct);
            AstraCliConsole.outputSuccess("Tenant '" + ct.getTenantName() + "' has being created.");
        } else {
            AstraCliConsole.outputSuccess("Tenant already existed (--if-not-exist)");
        }
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
    public void createPulsarConf(Tenant tenant) {
        PulsarShellUtils.generateConf(
                tenant.getCloudProvider() , 
                tenant.getCloudRegion(), 
                tenant.getPulsarToken(),
                getPulsarConfFile(tenant).getAbsolutePath());
        LoggerShell.info("Pulsar client.conf has been generated.");
    }
    
    /**
     * Start Pulsar shell as a Process.
     * 
     * @param options
     *      options from the command line
     * @param tenantName
     *      current tenant name
     * @throws TenantNotFoundException
     *      error if tenant is not found 
     * @throws CannotStartProcessException
     *      cannot start the process 
     * @throws FileSystemException
     *      cannot access configuration file
     */
    public void startPulsarShell(PulsarShellOptions options, String tenantName)
    throws TenantNotFoundException, CannotStartProcessException, FileSystemException {
        
        // Retrieve tenant information from devops Apis or exception
        Tenant tenant = getTenant(tenantName);
        
        // Download and install pulsar-shell tarball when needed
        if (!PulsarShellUtils.isPulsarShellInstalled()) {
            PulsarShellUtils.installPulsarShell();
        }
        
        // Generating configuration file if needed (~/.astra/luna streaming-shell-2.10.1.1/conf/...)
        createPulsarConf(tenant);
        
        try {
            AstraCliConsole.println("Pulsar-shell is starting please wait for connection establishment...");
            Process cqlShProcess = PulsarShellUtils.runPulsarShell(options, getPulsarConfFile(tenant));
            cqlShProcess.waitFor();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            LoggerShell.error("Cannot start Pulsar Shel :" + e.getMessage());
            throw new CannotStartProcessException("pulsar-shell", e);
        }
    }

    /**
     * Create keys relative to Streaming.
     *
     * @param tenantName
     *      tenant name
     * @param dest
     *      destination folder
     */
    public void generateDotEnvFile(String tenantName, String dest) {
        Tenant tenant = getTenant(tenantName);
        EnvFile envFile = new EnvFile(dest);
        // Tenant Information
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_STREAMING_NAME.name(), tenantName);
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_STREAMING_CLOUD.name(), tenant.getCloudProvider());
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_STREAMING_REGION.name(), tenant.getCloudRegion());
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_STREAMING_PULSAR_TOKEN.name(), tenant.getPulsarToken());
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_STREAMING_BROKER_URL.name(), tenant.getBrokerServiceUrl());
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_STREAMING_WEBSERVICE_URL.name(), tenant.getWebServiceUrl());
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_STREAMING_WEBSOCKET_URL.name(), tenant.getWebsocketUrl());
        envFile.save();
        LoggerShell.success("File '%s' has been created/amended".formatted(envFile.getDotenvFile().getAbsolutePath()) );
    }

}
    
