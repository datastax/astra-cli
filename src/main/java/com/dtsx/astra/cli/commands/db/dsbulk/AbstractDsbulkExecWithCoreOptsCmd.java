package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.core.CliConstants.$Db;
import com.dtsx.astra.cli.core.CliConstants.$Keyspace;
import com.dtsx.astra.cli.core.CliConstants.$Regions;
import com.dtsx.astra.cli.core.CliConstants.$Table;
import com.dtsx.astra.cli.core.completions.impls.DbNamesCompletion;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;

public abstract class AbstractDsbulkExecWithCoreOptsCmd extends AbstractDsbulkExecCmd {
    @Parameters(
        completionCandidates = DbNamesCompletion.class,
        description = "The name or ID of the Astra database to operate on",
        paramLabel = $Db.LABEL
    )
    protected DbRef $dbRef;

    @Option(
        names = { $Keyspace.LONG, $Keyspace.SHORT },
        description = "Keyspace used for loading or unloading data",
        paramLabel = $Keyspace.LABEL
    )
    public String $keyspace;
    
    @Option(
        names = { $Table.LONG, $Table.SHORT },
        description = "Table used for loading or unloading data.",
        paramLabel = $Table.LABEL
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
    public String $logDir;

    @Option(
        names = { $Regions.LONG, $Regions.SHORT },
        description = "The region to use. Uses the db's default region if not specified.",
        paramLabel = $Regions.LABEL
    )
    public Optional<RegionName> $region;
    
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
}
