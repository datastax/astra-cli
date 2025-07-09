package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.commands.db.AbstractDbSpecificCmd;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputHuman;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkExecResult;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.DsbulkInstallFailed;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.Executed;
import com.dtsx.astra.cli.operations.db.dsbulk.AbstractDsbulkExeOperation.ScbDownloadFailed;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;

public abstract class AbstractDsbulkExecCmd extends AbstractDbSpecificCmd<DsbulkExecResult> {
    @Option(
        names = { "-k", "--keyspace" },
        description = "Keyspace used for loading or unloading data",
        paramLabel = "KEYSPACE"
    )
    public String $keyspace;
    
    @Option(
        names = { "-t", "--table" },
        description = "Table used for loading or unloading data. Table names should not be quoted and are case-sensitive",
        paramLabel = "TABLE"
    )
    public String $table;

    @Option(
        names = { "--encoding" },
        description = { "The file name format to use when writing. This setting is ignored when reading and for non-file URLs", DEFAULT_VALUE },
        paramLabel = "ENCODING",
        defaultValue = "UTF-8"
    )
    public String $encoding;
    
    @Option(
        names = { "--max-concurrent-queries" },
        description = { "The maximum number of concurrent queries that should be carried in parallel", DEFAULT_VALUE },
        paramLabel = "QUERIES",
        defaultValue = "AUTO"
    )
    public String $maxConcurrentQueries;
      
    @Option(
        names = { "--log-dir" },
        description = { "Log directory for dsbulk operations", DEFAULT_VALUE },
        paramLabel = "DIRECTORY",
        defaultValue = "./logs"
    )
    protected String $logDir;
    
    @ArgGroup
    private @Nullable DsBulkConfigProvider $dsBulkConfigProvider;

    public static class DsBulkConfigProvider {
        @Option(
            names = { "--dsbulk-config" },
            description = "Configuration file for DSBulk loader options",
            paramLabel = "CONFIG_FILE"
        )
        public Optional<File> configFile;
        
        @Option(
            names = { "--dsbulk-flags", "-F" },
            description = "Additional flags to pass to DSBulk loader",
            paramLabel = "FLAGS"
        )
        public Map<String, String> flags;
    }

    protected Either<File, Map<String, String>> $configProvider() {
        return $dsBulkConfigProvider != null
            ? ($dsBulkConfigProvider.configFile
                .<Either<File, Map<String, String>>>map(Either::left)
                .orElseGet(() -> Either.right($dsBulkConfigProvider.flags)))
            : Either.right(emptyMap());
    }

    @Override
    protected final OutputHuman executeHuman(DsbulkExecResult result) {
        return switch (result) {
            case DsbulkInstallFailed(var msg) -> OutputAll.message(msg);
            case ScbDownloadFailed(var msg) -> OutputAll.message(msg);
            case Executed(var exitCode) -> AstraCli.exit(exitCode);
        };
    }
}
