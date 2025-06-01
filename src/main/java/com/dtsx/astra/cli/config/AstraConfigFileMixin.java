package com.dtsx.astra.cli.config;

import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.*;

import java.io.File;

public class AstraConfigFileMixin {
    @Option(names = { "--config-file", "-cf" })
    private File configFile;

    @Nullable
    private AstraConfig astraConfig;

    public AstraConfig getAstraConfig() {
        if (astraConfig == null) {
            this.astraConfig = AstraConfig.readAstraConfigFile(configFile);
        }
        return astraConfig;
    }
}
