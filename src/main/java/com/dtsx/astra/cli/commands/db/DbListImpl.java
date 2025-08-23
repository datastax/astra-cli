package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.operations.db.DbListOperation;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.PlatformChars;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.val;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.operations.db.DbListOperation.*;

public abstract class DbListImpl extends AbstractDbCmd<Stream<Database>> {
    @Option(names = { "--vector" }, description = "Only show vector-enabled databases")
    protected boolean $vectorOnly;

    @Override
    protected OutputJson executeJson(Supplier<Stream<Database>> result) {
        return OutputJson.serializeValue(result.get().toList());
    }

    @Override
    protected final OutputAll execute(Supplier<Stream<Database>> result) {
        val data = result.get()
            .map((db) -> Map.of(
                "Name", name(db),
                "Id", id(db),
                "Regions", regions(db),
                "Cloud", cloud(db),
                "V", vector(db),
                "Status", status(db)
            ))
            .toList();

        return new ShellTable(data).withColumns("Name", "Id", "Regions", "Cloud", "V", "Status");
    }

    private String name(Database db) {
        return db.getInfo().getName();
    }

    private String id(Database db) {
        return db.getId();
    }

    private List<String> regions(Database db) {
        return db.getInfo().getDatacenters().stream().map(Datacenter::getRegion).toList();
    }

    private String cloud(Database db) {
        return db.getInfo().getCloudProvider().name().toLowerCase();
    }

    private String vector(Database db) {
        return db.getInfo().getDbType() != null ? PlatformChars.PRESENCE_INDICATOR : "";
    }

    private String status(Database db) {
        return AstraColors.highlight(db.getStatus());
    }

    @Override
    protected DbListOperation mkOperation() {
        return new DbListOperation(dbGateway, new DbListRequest($vectorOnly));
    }
}
