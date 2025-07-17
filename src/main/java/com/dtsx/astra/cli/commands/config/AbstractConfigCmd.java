package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.config.AstraConfig;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Option;

import java.io.File;

public abstract class AbstractConfigCmd<OpRes> extends AbstractCmd<OpRes> {
    @Option(names = { "--config-file", "-cf" }, description = "The astrarc file to use", paramLabel = "PATH")
    private File configFile;

    @Nullable
    private AstraConfig astraConfig;

    public AstraConfig config(boolean createIfNotExists) {
        if (astraConfig == null) {
            this.astraConfig = AstraConfig.readAstraConfigFile(configFile, createIfNotExists);
        }
        return astraConfig;
    }
}
