package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.RenderableShellTable;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.DbGetOperation;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.*;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.operations.db.DbGetOperation.*;

@Command(
    name = "get",
    aliases = { "describe" },
    description = "Get information about a specific database."
)
@Example(
    comment = "Get information about a specific database",
    command = "${cli.name} db get my_db"
)
@Example(
    comment = "Get a specific attribute of a database",
    command = "${cli.name} db get my_db --key id"
)
public class DbGetCmd extends AbstractPromptForDbCmd<DbInfo> {
    public enum DbGetKeys {
        name,
        id,
        status,
        cloud,
        keyspace,
        keyspaces,
        region,
        regions,
        creation_time,
        vector
    }

    @Option(
        names = { "-k", "--key" },
        description = "Specific database attribute to retrieve",
        paramLabel = "KEY"
    )
    public Optional<DbGetKeys> $key;

    @Override
    protected final OutputJson executeJson(Supplier<DbInfo> result) {
        return switch (result.get()) {
            case DbInfoFull info -> OutputJson.serializeValue(info.database());
            case DbInfoValue(var value) -> OutputJson.serializeValue(value);
        };
    }

    @Override
    protected final OutputAll execute(Supplier<DbInfo> result) {
        return switch (result.get()) {
            case DbInfoFull info -> mkTable(info.database());
            case DbInfoValue(var value) -> OutputAll.serializeValue(value);
        };
    }

    private RenderableShellTable mkTable(Database dbInfo) {
        return ShellTable.forAttributes(new LinkedHashMap<>() {{
            put("Name", dbInfo.getInfo().getName());
            put("ID", dbInfo.getId());
            put("Cloud Provider", dbInfo.getInfo().getCloudProvider());
            put("Region", dbInfo.getInfo().getRegion());
            put("Status", dbInfo.getStatus());
            put("Vector", Optional.ofNullable(dbInfo.getInfo().getDbType()).orElse("").equals("vector") ? "Enabled" : "Disabled");
            put("Default Keyspace", dbInfo.getInfo().getKeyspace());
            put("Creation Time", dbInfo.getCreationTime());
            put("Keyspaces", Objects.requireNonNullElse(dbInfo.getInfo().getKeyspaces(), Set.of()).stream().sorted().toList());
            put("Regions", Objects.requireNonNullElse(dbInfo.getInfo().getDatacenters(), Set.<Datacenter>of()).stream().map(Datacenter::getRegion).sorted().toList());
        }});
    }

    @Override
    protected Operation<DbInfo> mkOperation() {
        return new DbGetOperation(dbGateway, new DbGetRequest($dbRef, $key));
    }

    @Override
    protected String dbRefPrompt() {
        return "Select the database to get information about";
    }
}
