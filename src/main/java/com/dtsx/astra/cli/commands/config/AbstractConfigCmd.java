package com.dtsx.astra.cli.commands.config;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.core.CliConstants.$ConfigFile;
import com.dtsx.astra.cli.core.config.AstraConfig;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Option;

import java.nio.file.Path;

public abstract class AbstractConfigCmd<OpRes> extends AbstractCmd<OpRes> {
    @Option(
        names = { $ConfigFile.LONG, $ConfigFile.SHORT },
        description = "The astrarc file to use",
        paramLabel = $ConfigFile.LABEL
    )
    private Path $configFile;

    @Nullable
    private AstraConfig cachedAstraConfig;

    public AstraConfig config(boolean createIfNotExists) {
        if (cachedAstraConfig == null) {
            this.cachedAstraConfig = AstraConfig.readAstraConfigFile($configFile, createIfNotExists);
        }
        return cachedAstraConfig;
    }
}
