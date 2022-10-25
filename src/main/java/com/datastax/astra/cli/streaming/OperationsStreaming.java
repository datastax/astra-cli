package com.datastax.astra.cli.streaming;

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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.datastax.astra.cli.core.CliContext;
import com.datastax.astra.cli.core.ExitCode;
import com.datastax.astra.cli.core.exception.CannotStartProcessException;
import com.datastax.astra.cli.core.exception.FileSystemException;
import com.datastax.astra.cli.core.out.*;
import com.datastax.astra.cli.streaming.StreamingGetCmd.StreamingGetKeys;
import com.datastax.astra.cli.streaming.exception.TenantAlreadyExistException;
import com.datastax.astra.cli.streaming.exception.TenantNotFoundException;
import com.datastax.astra.cli.streaming.pulsarshell.PulsarShellOptions;
import com.datastax.astra.cli.streaming.pulsarshell.PulsarShellUtils;
import com.datastax.astra.cli.utils.EnvFile;
import com.datastax.astra.sdk.streaming.StreamingClient;
import com.datastax.astra.sdk.streaming.TenantClient;
import com.datastax.astra.sdk.streaming.domain.CreateTenant;
import com.datastax.astra.sdk.streaming.domain.Tenant;

/**
 * Utility class for command `streaming`
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class OperationsStreaming {

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
    public static final String COLUMN_STATUS = "Status";
    /** Working object. */
    static final String TENANT = "Tenant";
    
    /** limit resource usage by caching tenant clients. */
    private static final Map<String, TenantClient> cacheTenantClient = new HashMap<>();
    
    /**
     * Hide default constructor
     */
    private OperationsStreaming() {}
    
    /**
     * Syntax Sugar to work with Streaming Devops Apis.
     * 
     * @return
     *      streaming tenant.
     */
    private static StreamingClient streamingClient() {
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
    private static TenantClient tenantClient(String tenantName) {
        return cacheTenantClient.computeIfAbsent(tenantName, (t) -> streamingClient().tenant(t));
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
    private static Tenant getTenant(String tenantName) 
    throws TenantNotFoundException {
        return tenantClient(tenantName)
                .find()
                .orElseThrow(() -> new TenantNotFoundException(tenantName));
    }
    
    /**
     * List Tenants.
     */
    public static void listTenants() {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_NAME,    20);
        sht.addColumn(COLUMN_CLOUD,   10);
        sht.addColumn(COLUMN_REGION,  15);
        sht.addColumn(COLUMN_STATUS,  15);
        streamingClient()
           .tenants()
           .forEach(tnt -> {
                Map <String, String> rf = new HashMap<>();
                rf.put(COLUMN_NAME,   tnt.getTenantName());
                rf.put(COLUMN_CLOUD,  tnt.getCloudProvider());
                rf.put(COLUMN_REGION, tnt.getCloudRegion());
                rf.put(COLUMN_STATUS, tnt.getStatus());
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
    public static void showTenant(String tenantName, StreamingGetKeys key)
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
    public static void deleteTenant(String tenantName) 
    throws TenantNotFoundException {
        getTenant(tenantName);
        tenantClient(tenantName).delete();
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
    public static void showTenantStatus(String tenantName)
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
    public static void showTenantExistence(String tenantName) {
        if (tenantClient(tenantName).exist()) {
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
    public static void showTenantPulsarToken(String tenantName)
    throws TenantNotFoundException {
       AstraCliConsole.println(getTenant(tenantName).getPulsarToken());
    }
    
    /**
     * Create a streaming tenant.
     *
     * @param ct
     *      tenant creation request
     * @throws TenantAlreadyExistException
     *      already exist exception 
     */
    public static void createStreamingTenant(CreateTenant ct) 
    throws TenantAlreadyExistException {
        if (tenantClient(ct.getTenantName()).exist()) {
            throw new TenantAlreadyExistException(ct.getTenantName());
        }
        streamingClient().createTenant(ct);
        AstraCliConsole.outputSuccess("Tenant '" + ct.getTenantName() + "' has being created.");
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
    public static void startPulsarShell(PulsarShellOptions options, String tenantName) 
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
    public static void generateDotEnvFile(String tenantName, String dest) {
        Tenant tenant = getTenant(tenantName);
        EnvFile envFile = new EnvFile(dest);
        // Tenant Information
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_STREAMING_NAME, tenantName);
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_STREAMING_CLOUD, tenant.getCloudProvider());
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_STREAMING_REGION, tenant.getCloudRegion());
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_STREAMING_PULSAR_TOKEN, tenant.getPulsarToken());
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_STREAMING_BROKER_URL, tenant.getBrokerServiceUrl());
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_STREAMING_WEBSERVICE_URL, tenant.getWebServiceUrl());
        envFile.getKeys().put(EnvFile.EnvKey.ASTRA_STREAMING_WEBSOCKET_URL, tenant.getWebsocketUrl());
        envFile.save();
        LoggerShell.success("File '%s' has been created/amended".formatted(envFile.getDotenvFile().getAbsolutePath()) );
    }

}
    
