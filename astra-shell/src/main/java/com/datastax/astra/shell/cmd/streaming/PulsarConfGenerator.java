package com.datastax.astra.shell.cmd.streaming;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Wrap the pulsar configuration for the tenant.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class PulsarConfGenerator {
      
    /**
     * Hide default constructor
     */
    private PulsarConfGenerator() {}
    
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
            pw.flush();
        } catch (IOException e1) {
            throw new IllegalStateException("Cannot generate configuration file.");
        } finally {
            try {
                if (pw != null)        pw.close();
                if (fileWriter!= null) fileWriter .close();
            } catch (IOException e) {}
        }
    }
    
    
    

}
