package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.table.TableListOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.operations.db.table.TableListOperation.TableListRequest;
import static com.dtsx.astra.cli.operations.db.table.TableListOperation.TableListResult;
import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    name = "list-tables",
    description = "List the tables in the specified database and keyspace"
)
@Example(
    comment = "List all tables in the default keyspace",
    command = "${cli.name} db list-tables my_db"
)
@Example(
    comment = "List all tables in a specific keyspace",
    command = "${cli.name} db list-tables my_db -k my_keyspace"
)
@Example(
    comment = "List all tables in all keyspaces",
    command = "${cli.name} db list-tables my_db --all"
)
public class TableListCmd extends AbstractTableCmd<Stream<TableListResult>> {
    @Option(
        names = { "--all", "-a" },
        description = "List tables in all keyspaces",
        defaultValue = "false"
    )
    public boolean $all;

    @Override
    protected boolean parseKeyspaceRef() {
        return !$all;
    }

    @Override
    protected void prelude() {
        super.prelude();

        if ($all && $keyspaceRef != null) {
            throw new ParameterException(spec.commandLine(), "Cannot use --all with a specific keyspace (the -k flag)");
        }
    }

    @Override
    protected final OutputJson executeJson(Supplier<Stream<TableListResult>> result) {
        if ($all) {
            return OutputJson.serializeValue(result.get()
                .map(res -> sequencedMapOf(
                    "keyspace", res.keyspace(),
                    "tables", res.tables()
                ))
                .toList());
        }

        return OutputJson.serializeValue(result.get().toList().getFirst().tables());
    }

    @Override
    public final OutputAll execute(Supplier<Stream<TableListResult>> result) {
        val data = result.get()
            .flatMap((res) -> (
                res.tables().stream()
                    .map((desc) -> sequencedMapOf(
                        "Keyspace", res.keyspace(),
                        "Name", desc.getName()
                    ))
            ))
            .toList();

        if ($all) {
            return new ShellTable(data).withColumns("Keyspace", "Name");
        } else {
            return new ShellTable(data).withColumns("Name");
        }
    }

    @Override
    protected Operation<Stream<TableListResult>> mkOperation() {
        return new TableListOperation(tableGateway, keyspaceGateway, new TableListRequest(
            $dbRef,
            Optional.ofNullable($keyspaceRef)
        ));
    }
}
