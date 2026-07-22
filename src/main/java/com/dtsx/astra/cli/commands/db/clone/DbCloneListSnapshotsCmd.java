package com.dtsx.astra.cli.commands.db.clone;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.clone.DbCloneListSnapshotsOperation;
import com.dtsx.astra.cli.operations.db.clone.DbCloneListSnapshotsOperation.DbCloneListSnapshotsRequest;
import com.dtsx.astra.sdk.db.domain.DatabaseSnapshot;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    name = "list-snapshots",
    description = "List available snapshots for a database that can be used for cloning"
)
@Example(
    comment = "List available snapshots for a database interactively",
    command = "${cli.name} db clone list-snapshots"
)
@Example(
    comment = "List available snapshots for a specific database",
    command = "${cli.name} db clone list-snapshots my_db"
)
public class DbCloneListSnapshotsCmd extends AbstractDbCloneCmd<Stream<DatabaseSnapshot>> {
    @Option(
        names = { "-r", "--region" },
        description = "Filter snapshots by a specific region",
        paramLabel = "REGION"
    )
    public Optional<String> $region = Optional.empty();

    @Option(
        names = { "--from" },
        description = "Start date for snapshot filtering (e.g. 2023-01-01)",
        paramLabel = "START_DATE"
    )
    public Optional<String> $from = Optional.empty(); // TODO see if I can use instants or something

    @Option(
        names = { "--to" },
        description = "End date for snapshot filtering (e.g. 2023-12-31)",
        paramLabel = "END_DATE"
    )
    public Optional<String> $to = Optional.empty();

    @Override
    protected OutputJson executeJson(Supplier<Stream<DatabaseSnapshot>> result) {
        return OutputJson.serializeValue(result);
    }

    @Override
    protected OutputAll execute(Supplier<Stream<DatabaseSnapshot>> result) {
        val items = result.get()
            .map(s -> sequencedMapOf(
                "ID", s.getId(),
                "Time", s.getTime()
            ))
            .toList();

        return new ShellTable(items).withColumns("ID", "Time");
    }

    @Override
    protected Operation<Stream<DatabaseSnapshot>> mkOperation() {
        return new DbCloneListSnapshotsOperation(dbCloneGateway, new DbCloneListSnapshotsRequest($dbRef, $region, $from, $to));
    }

    @Override
    protected String dbRefPrompt() {
        return "Select the source database to list snapshots for";
    }
}
