package com.dtsx.astra.cli.streaming.pulsarshell;

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

import com.dtsx.astra.cli.core.exception.ConfigurationException;
import com.dtsx.astra.cli.core.exception.FileSystemException;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.utils.AstraCliUtils;
import com.dtsx.astra.cli.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities to work with Pulsar.
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
     * Hide default constructor
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
        try (FileWriter fileWriter = new FileWriter(destination);
             PrintWriter pw = new PrintWriter(fileWriter)) {
            pw.printf("webServiceUrl=https://pulsar-%s-%s.api.streaming.datastax.com%n", cloudProvider, cloudRegion);
            pw.printf("brokerServiceUrl=pulsar+ssl://pulsar-%s-%s.streaming.datastax.com:6651%n", cloudProvider, cloudRegion);
            pw.printf("authPlugin=%s%n", "org.apache.pulsar.client.impl.auth.AuthenticationToken");
            pw.printf("authParams=token:%s%n", pulsarToken);
            pw.printf("tlsAllowInsecureConnection=%b%n", false);
            pw.printf("tlsEnableHostnameVerification=%b%n", true);
            pw.printf("useKeyStoreTls=%b%n", false);
            pw.printf("tlsTrustStoreType=%s%n", "JKS");
            pw.printf("tlsTrustStorePath=%s%n", "");
            pw.printf("tlsTrustStorePassword=%s%n", "");
        } catch (IOException e1) {
            throw new IllegalStateException("Cannot generate configuration file.");
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
     * Download tar archive and unzip.
     *
     * @throws FileSystemException
     *      file system exception 
     */
    public static void installPulsarShell() 
    throws FileSystemException {
        try {
            LoggerShell.info("Downloading PulsarShell, please wait...");
            String destination = AstraCliUtils.ASTRA_HOME + File.separator + LUNA_TARBALL;
            FileUtils.downloadFile(LUNA_URL, destination);
            File tarArchive = new File (destination);

            LoggerShell.info("Installing  archive, please wait...");
            FileUtils.extractTarArchiveInAstraCliHome(tarArchive);

            File pulsarShellFile = new File(AstraCliUtils.ASTRA_HOME + File.separator
                    + LUNA_FOLDER + File.separator
                    + "bin" + File.separator
                    + "pulsar-shell");
            if (!pulsarShellFile.setExecutable(true, false)) {
                throw new FileSystemException("Cannot make pulsar-shell executable. ");
            }
            Files.delete(Paths.get(destination));
        } catch (IOException e) {
            throw new FileSystemException("Cannot install Pulsar-Shell :" + e.getMessage(), e);
        }
    }

    /**
     * Start Pulsar Shell as a sub process in the CLI.
     * 
     * @param options
     *      command to start pulsar-shell
     * @param configFile
     *      configuration file associated with the current tenant
     * @return
     *      unix process for pulsar-shell
     * @throws IOException
     *      errors occurred
     * @throws ConfigurationException
     *      starting pulsar shell 
     */
    public static Process runPulsarShell(PulsarShellOptions options, File configFile)
    throws IOException, ConfigurationException {
        
        if (!configFile.exists()) {
            LoggerShell.error("Client.conf '" + configFile.getAbsolutePath() + "' has not been found.");
            throw new ConfigurationException(configFile.getAbsolutePath());
        }
        
        List<String> pulsarShCommand = new ArrayList<>();
        pulsarShCommand.add(AstraCliUtils.ASTRA_HOME + File.separator + LUNA_FOLDER +
                File.separator + "bin" +
                File.separator + "pulsar-shell");
        
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

        LoggerShell.debug("RUNNING: " + StringUtils.join(pulsarShCommand, " "));
        ProcessBuilder pb =  new ProcessBuilder(pulsarShCommand.toArray(new String[0]));
        pb.inheritIO();
        return pb.start();
    }
    
}
