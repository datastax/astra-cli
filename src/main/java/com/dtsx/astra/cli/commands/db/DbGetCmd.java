package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputJson;
import com.dtsx.astra.cli.core.output.table.RenderableShellTable;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.DbGetOperation;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.commands.db.DbGetCmd.DbGetKeys.*;
import static com.dtsx.astra.cli.operations.db.DbGetOperation.DbGetRequest;
import static com.dtsx.astra.cli.operations.db.DbGetOperation.DbGetResult;

@Command(
    name = "get",
    aliases = { "describe" },
    description = "Get information about a specific database."
)
@Example(
    comment = "Get information about a specific database",
    command = "astra db get my_db"
)
@Example(
    comment = "Get a specific attribute of a database",
    command = "astra db get my_db -k id"
)
public class DbGetCmd extends AbstractDbSpecificCmd<DbGetResult> {
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
        paramLabel = "<key>"
    )
    public Optional<DbGetKeys> $key;

    @Override
    public OutputJson executeJson(DbGetResult result) {
        if ($key.isPresent()) {
            return execute(result);
        }
        return OutputJson.serializeValue(result.database());
    }

    @Override
    protected final OutputAll execute(DbGetResult result) {
        val dbInfo = result.database();

        return $key
            .map((k) -> dbInfo4Key(dbInfo, k))
            .map(OutputAll::serializeValue)
            .orElseGet(() -> this.mkTable(dbInfo));
    }

    private RenderableShellTable mkTable(Database dbInfo) {
        return new ShellTable(List.of(
            ShellTable.attr("Name", dbInfo4Key(dbInfo, name)),
            ShellTable.attr("id", dbInfo4Key(dbInfo, id)),
            ShellTable.attr("Cloud", dbInfo4Key(dbInfo, cloud)),
            ShellTable.attr("Region", dbInfo4Key(dbInfo, region)),
            ShellTable.attr("Status", dbInfo4Key(dbInfo, status)),
            ShellTable.attr("Vector", dbInfo4Key(dbInfo, vector).equals(true) ? "Enabled" : "Disabled"),
            ShellTable.attr("Default Keyspace", dbInfo4Key(dbInfo, keyspace)),
            ShellTable.attr("Creation Time", dbInfo4Key(dbInfo, creation_time)),
            ShellTable.attr("Keyspaces", dbInfo4Key(dbInfo, keyspaces)),
            ShellTable.attr("Regions", dbInfo4Key(dbInfo, regions))
        )).withAttributeColumns();
    }

    private Object dbInfo4Key(Database dbInfo, DbGetKeys key) {
        return switch (key) {
            case name -> dbInfo.getInfo().getName();
            case id -> dbInfo.getId();
            case status -> dbInfo.getStatus();
            case cloud -> dbInfo.getInfo().getCloudProvider();
            case keyspace -> dbInfo.getInfo().getKeyspace();
            case keyspaces -> dbInfo.getInfo().getKeyspaces().stream().toList();
            case region -> dbInfo.getInfo().getRegion();
            case regions -> dbInfo.getInfo().getDatacenters().stream().map(Datacenter::getRegion).toList();
            case creation_time -> dbInfo.getCreationTime();
            case vector -> dbInfo.getInfo().getDbType().equals("vector");
        };
    }

    @Override
    protected DbGetOperation mkOperation() {
        return new DbGetOperation(dbGateway, new DbGetRequest($dbRef));
    }
}
