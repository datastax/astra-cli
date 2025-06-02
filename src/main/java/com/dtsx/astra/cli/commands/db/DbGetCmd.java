package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.output.output.OutputAll;
import com.dtsx.astra.cli.output.output.OutputJson;
import com.dtsx.astra.cli.output.table.RenderableShellTable;
import com.dtsx.astra.cli.output.table.ShellTable;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.commands.db.DbGetCmd.DbGetKeys.*;

@Command(
    name = "get",
    aliases = { "describe" }
)
public class DbGetCmd extends AbstractDbSpecificCmd {
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

    @Option(names = { "-k", "--key" }, description = "Specific database attribute to retrieve", paramLabel = "<key>")
    private Optional<DbGetKeys> key;

    @Override
    public OutputJson executeJson() {
        if (key.isPresent()) {
            return execute();
        }
        return OutputJson.serializeValue(dbInfo());
    }

    @Override
    public OutputAll execute() {
        return key
            .map(this::dbInfo4Key)
            .map(OutputAll::serializeValue)
            .orElseGet(this::mkTable);
    }

    private RenderableShellTable mkTable() {
        return new ShellTable(List.of(
            ShellTable.attr("Name", dbInfo4Key(name)),
            ShellTable.attr("id", dbInfo4Key(id)),
            ShellTable.attr("Cloud", dbInfo4Key(cloud)),
            ShellTable.attr("Region", dbInfo4Key(region)),
            ShellTable.attr("Status", dbInfo4Key(status)),
            ShellTable.attr("Vector", dbInfo4Key(vector).equals(true) ? "Enabled" : "Disabled"),
            ShellTable.attr("Default Keyspace", dbInfo4Key(keyspace)),
            ShellTable.attr("Creation Time", dbInfo4Key(creation_time)),
            ShellTable.attr("Keyspaces", dbInfo4Key(keyspaces)),
            ShellTable.attr("Regions", dbInfo4Key(regions))
        )).withAttributeColumns();
    }

    private @Nullable Database cachedDbInfo;

    private Database dbInfo() {
        if (cachedDbInfo == null) {
            cachedDbInfo = dbService.getDbInfo(dbName);
        }
        return cachedDbInfo;
    }

    private Object dbInfo4Key(DbGetKeys key) {
        val dbInfo = dbInfo();

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
}
