package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.core.CliConstants.$Db;
import com.dtsx.astra.cli.core.CliConstants.$Keyspace;
import com.dtsx.astra.cli.core.CliConstants.$Regions;
import com.dtsx.astra.cli.core.CliConstants.$Table;
import com.dtsx.astra.cli.core.completions.impls.DbNamesCompletion;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionRef;
import com.dtsx.astra.cli.core.output.prompters.specific.DbRefPrompter;
import com.dtsx.astra.cli.utils.CliUtils;
import lombok.val;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractDsbulkExecWithCoreOptsCmd extends AbstractDsbulkExecCmd {
    @Parameters(
        arity = "0..1",
        completionCandidates = DbNamesCompletion.class,
        description = "The name or ID of the Astra database to operate on",
        paramLabel = $Db.LABEL
    )
    private Optional<DbRef> $maybeDbRef;

    @Option(
        names = { $Keyspace.LONG, $Keyspace.SHORT },
        description = "Keyspace used for loading or unloading data",
        paramLabel = $Keyspace.LABEL
    )
    public Optional<String> $keyspace;
    
    @Option(
        names = { $Table.LONG, $Table.SHORT },
        description = "Table used for loading or unloading data.",
        paramLabel = $Table.LABEL
    )
    public Optional<String> $table;

    @Option(
        names = { "--encoding" },
        description = "The file name format to use when writing. This setting is ignored when reading and for non-file URLs",
        paramLabel = "ENCODING",
        defaultValue = "UTF-8"
    )
    public String $encoding;
    
    @Option(
        names = { "--max-concurrent-queries" },
        description = "The maximum number of concurrent queries that should be carried in parallel",
        paramLabel = "QUERIES",
        defaultValue = "AUTO"
    )
    public String $maxConcurrentQueries;
      
    @Option(
        names = { "--log-dir" },
        description = "Log directory for dsbulk operations",
        paramLabel = "DIRECTORY",
        defaultValue = "./logs"
    )
    public String $logDir;

    @Option(
        names = { $Regions.LONG, $Regions.SHORT },
        description = "The region to use. Uses the db's default region if not specified.",
        paramLabel = $Regions.LABEL
    )
    public Optional<String> $regionName;

    @Option(
        names = { "--dsbulk-config" },
        description = "Configuration file for DSBulk loader options",
        paramLabel = "CONFIG_FILE"
    )
    public Optional<Path> $configFile;

    @Option(
        names = { "--dsbulk-flag", "-F" },
        description = "Additional flags to pass to DSBulk loader, can be specified multiple times (e.g., -F '--key1=value1' -F '--key2')",
        paramLabel = "FLAGS",
        mapFallbackValue = Option.NULL_VALUE
    )
    public Map<String, String> $legacyExtraArgs = Map.of();

    @Parameters(
        paramLabel = "ARGS",
        description = "Verbatim arguments to pass to dsbulk directly (anything after '--' is passed through)"
    )
    public List<String> $extraArgs = List.of();

    public DbRef $dbRef;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();

        $dbRef = $maybeDbRef.orElseGet(() -> (
            DbRefPrompter.prompt(ctx, dbGateway, "Select the database to work with:", db -> db, (b) -> b.fallbackIndex(0).fix(originalArgs(), "<db>"))
        ));
    }

    protected Optional<RegionRef> $region() {
        return RegionRef.mustParse($dbRef, $regionName);
    }

    protected List<String> $flags() {
        val allArgs = new ArrayList<String>();

        if (!$legacyExtraArgs.isEmpty()) {
            ctx.log().warn("The @!--dsbulk-flags!@ option is deprecated. Please use @!--dsbulk-flag/-F!@ instead as a direct replacement.");

            $legacyExtraArgs.forEach((k, v) -> {
                allArgs.add(k);
                if (v != null) {
                    allArgs.add(v);
                }
            });
        }

        $extraArgs = CliUtils.removeDbFromExtraArgs($extraArgs, "db dsbulk <cmd> -- --key1 value1 --key2");
        allArgs.addAll($extraArgs);

        return allArgs;
    }
}
