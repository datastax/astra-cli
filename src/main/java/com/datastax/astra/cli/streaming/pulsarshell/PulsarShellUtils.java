package com.datastax.astra.cli.streaming.pulsarshell;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.datastax.astra.cli.core.exception.ConfigurationException;
import com.datastax.astra.cli.core.exception.FileSystemException;
import com.datastax.astra.cli.core.out.LoggerShell;
import com.datastax.astra.cli.utils.AstraCliUtils;
import com.datastax.astra.cli.utils.FileUtils;
import com.datastax.astra.sdk.streaming.domain.Tenant;

/**
 * Utilities to work with Pulsar
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class PulsarShellUtils {
    
    /** Version Number. */
    public static final String LUNA_VERSION = AstraCliUtils.readProperty("pulsar.shell.version");
    
    /** Archive name. */
    public static final String LUNA_TARBALL = "lunastreaming-shell-" + LUNA_VERSION + "-bin.tar.gz";
    
    /** Archive name. */
    public static final String LUNA_FOLDER = "lunastreaming-shell-" + LUNA_VERSION + "";
    
    /** Pulsar. */
    public static final String LUNA_URL = AstraCliUtils.readProperty("pulsar.shell.url") + LUNA_TARBALL;
    
    /**
     * Hide default construtor
     */
    private PulsarShellUtils() {}
    
    /**
     * Generate content of the File.
     * 
     * @param cloudProvider
     *      cloud provider
     * @param cloudRegion
     *      cloud region
     * @param pulsarToken
     *      pulsar token
     * @param destination
     *      destination file
     */
    public static void generateConf(String cloudProvider, String cloudRegion, String pulsarToken, String destination) {
        FileWriter fileWriter = null;
        PrintWriter pw        = null;
        try {
            fileWriter = new FileWriter(destination);
            pw = new PrintWriter(fileWriter);
            pw.printf("webServiceUrl=https://pulsar-%s-%s.api.streaming.datastax.com\n", cloudProvider, cloudRegion);
            pw.printf("brokerServiceUrl=pulsar+ssl://pulsar-%s-%s.streaming.datastax.com:6651\n", cloudProvider, cloudRegion);
            pw.printf("authPlugin=org.apache.pulsar.client.impl.auth.AuthenticationToken\n");
            pw.printf("authParams=token:%s\n", pulsarToken);
            pw.printf("tlsAllowInsecureConnection=%b\n", false);
            pw.printf("tlsEnableHostnameVerification=%b\n", true);
            pw.printf("useKeyStoreTls=%b\n", false);
            pw.printf("tlsTrustStoreType=%s\n", "JKS");
            pw.printf("tlsTrustStorePath=\n");
            pw.printf("tlsTrustStorePassword=\n");
            //pw.flush();
        } catch (IOException e1) {
            throw new IllegalStateException("Cannot generate configuration file.");
        } finally {
            try {
                if (pw != null)        pw.close();
                if (fileWriter!= null) fileWriter .close();
            } catch (IOException e) {}
        }
    }
    
    /**
     * Configuration folder.
     * 
     * @return
     *      folder for configuration
     */
    public static String getConfigurationFolder() {
        return AstraCliUtils.ASTRA_HOME + 
                File.separator + LUNA_FOLDER + 
                File.separator + "conf";
    }
    
    /**
     * Check if lunastreaming-shell has been installed.
     *
     * @return
     *      if the folder exist
     */
    public static boolean isPulsarShellInstalled() {
       File pulsarShellFolder = new File(AstraCliUtils.ASTRA_HOME + File.separator + LUNA_FOLDER);
       return pulsarShellFolder.exists() && pulsarShellFolder.isDirectory();
    }
    
    /**
     * Download targz and unzip.
     *
     * @throws FileSystemException
     *      file system exception 
     */
    public static void installPulsarShell() 
    throws FileSystemException {
        if (!isPulsarShellInstalled()) {
            LoggerShell.success("pulsar-shell first launch, downloading (~ 60MB), please wait...");
            String destination = AstraCliUtils.ASTRA_HOME + File.separator + LUNA_TARBALL;
            FileUtils.downloadFile(LUNA_URL, destination);
            File pulsarShelltarball = new File (destination);
            if (pulsarShelltarball.exists()) {
                LoggerShell.info("File Downloaded. Extracting archive, please wait it can take a minute...");
                try {
                    FileUtils.extactTargzInAstraCliHome(pulsarShelltarball);
                    if (isPulsarShellInstalled()) {
                        // Change file permission
                        File pulsarShellFile = new File(AstraCliUtils.ASTRA_HOME + File.separator  
                                + LUNA_FOLDER + File.separator 
                                + "bin" + File.separator  
                                + "pulsar-shell");
                        if (!pulsarShellFile.setExecutable(true, false)) {
                            throw new FileSystemException("Cannot set pulsar-shell file as executable");
                        }
                        if (!pulsarShellFile.setReadable(true, false)) {
                            throw new FileSystemException("Cannot set pulsar-shell file as readable");
                        }
                        if (!pulsarShellFile.setWritable(true, false)) {
                            throw new FileSystemException("Cannot set pulsar-shell file as writable");
                        }
                        LoggerShell.success("pulsar-shell has been installed");
                        if (!pulsarShelltarball.delete()) {
                            LoggerShell.warning("Pulsar-shell Tar archived was not deleted");
                        }
                    }
                } catch (IOException e) {
                    LoggerShell.error("Cannot extract tar archive:" + e.getMessage());
                    throw new FileSystemException("Cannot extract tar archive:" + e.getMessage(), e);
                }
            }
        } else {
            LoggerShell.info("pulsar-shell is already installed");
        }
    }

    /**
     * Start Pulsar Shell as a sub process in the CLI.
     * 
     * @param options
     *      command to start pulsar-shell
     * @param tenant
     *      current tenant
     * @param configFile
     *      configuration file associated with the current tenant
     * @return
     *      unix process for pulsar-shell
     * @throws IOException
     *      errors occured
     * @throws ConfigurationException
     *      starting pulsar shell 
     */
    public static Process runPulsarShell(PulsarShellOptions options, Tenant tenant, File configFile) 
    throws IOException, ConfigurationException {
        
        if (!configFile.exists()) {
            LoggerShell.error("Client.conf '" + configFile.getAbsolutePath() + "' has not been found.");
            throw new ConfigurationException(configFile.getAbsolutePath());
        }
        
        List<String> pulsarShCommand = new ArrayList<>();
        pulsarShCommand.add(new StringBuilder()
                .append(AstraCliUtils.ASTRA_HOME + File.separator + LUNA_FOLDER)
                .append(File.separator + "bin")
                .append(File.separator + "pulsar-shell")
                .toString());
        
        // Enforcing usage of generated file
        pulsarShCommand.add("--config");
        pulsarShCommand.add(configFile.getAbsolutePath());
        
        if (options.isNoProgress()) {
            pulsarShCommand.add("--no-progress");
        }
        if (options.isFailOnError()) {
            pulsarShCommand.add("--fail-on-error");
        }
        if (options.getExecute() != null) {
            pulsarShCommand.add("--execute-command");
            pulsarShCommand.add(options.getExecute());
        }
        if (options.getFileName() != null) {
            pulsarShCommand.add("--filename");
            pulsarShCommand.add(options.getFileName());
        }
        
        LoggerShell.info("RUNNING: " + StringUtils.join(pulsarShCommand, " "));
        ProcessBuilder pb =  new ProcessBuilder(pulsarShCommand.toArray(new String[0]));
        pb.inheritIO();
        return pb.start();
    }
    
}
