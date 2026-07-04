package com.dtsx.astra.cli.commands.db.dataapi;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.commands.db.AbstractPromptForDbCmd;
import com.dtsx.astra.cli.core.CliConstants.$Collection;
import com.dtsx.astra.cli.core.CliConstants.$Keyspace;
import com.dtsx.astra.cli.core.CliConstants.$Regions;
import com.dtsx.astra.cli.core.CliConstants.$Table;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.dataapi.DbDataAPIExecOperation;
import com.dtsx.astra.cli.operations.db.dataapi.DbDataAPIExecOperation.*;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Command(
    name = "repl",
    description = "Start an interactive Data API REPL for a database"
)
public class DataAPIReplCmd extends AbstractPromptForDbCmd<DataAPIExecResult> {
    @Option(
        names = { "-l", "--lang" },
        description = "The client language to start the REPL in (default: ${DEFAULT-VALUE}). Valid values: ${COMPLETION-CANDIDATES}",
        defaultValue = "js"
    )
    public Language $language;

    @Option(
        names = { "-a", "--artifacts" },
        split = ",",
        description = "Additional packages to include in the REPL (e.g., pandas for Python, lodash for JS)"
    )
    public List<String> $packages = new ArrayList<>();

    @Option(
        names = { "-e", "--extra" },
        description = "Code to execute after starting the REPL (e.g., to import modules or set up the environment)"
    )
    public String $extraCode = "";

    @Option(
        names = { $Regions.LONG, $Regions.SHORT },
        description = "The region to use",
        paramLabel = $Regions.LABEL
    )
    protected Optional<RegionName> $region;

    @Option(
        names = { $Keyspace.LONG, $Keyspace.SHORT },
        description = "The keyspace to use",
        paramLabel = $Keyspace.LABEL
    )
    public Optional<String> $keyspaceName;

    @Option(
        names = { $Collection.LONG, $Collection.SHORT },
        description = "The collection to use",
        paramLabel = $Collection.LABEL
    )
    public Optional<String> $collectionName;

    @Option(
        names = { $Table.LONG, $Table.SHORT },
        description = "The table to use",
        paramLabel = $Table.LABEL
    )
    public Optional<String> $tableName;

    @Override
    protected OutputHuman executeHuman(Supplier<DataAPIExecResult> resultSupplier) {
        return switch (resultSupplier.get()) {
            case Executed e -> AstraCli.exit(e.exitCode());
            case InvalidDbStatus e -> {
                ctx.log().exception("Cannot start REPL: database is in status " + e.status());
                yield AstraCli.exit(1);
            }
            case OperationFailed e -> {
                ctx.log().exception("Failed to start REPL: " + e.error());
                yield AstraCli.exit(1);
            }
        };
    }

    @Override
    protected Operation<DataAPIExecResult> mkOperation() {
        return new DbDataAPIExecOperation(ctx, dbGateway, new DbDataAPIExecRequest(
            $language,
            $dbRef,
            $region,
            $keyspaceName,
            $collectionName,
            $tableName,
            profile(),
            $packages,
            $extraCode
        ));
    }

    @Override
    protected String dbRefPrompt() {
        return "Select the database to connect to with the REPL:";
    }
}
