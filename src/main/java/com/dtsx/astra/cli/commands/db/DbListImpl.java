package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.operations.db.DbListOperation;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.val;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Map;

import static com.dtsx.astra.cli.operations.db.DbListOperation.*;

public abstract class DbListImpl extends AbstractDbCmd<List<Database>> {
    @Option(names = "--vector", description = "Only show vector-enabled databases")
    protected boolean $vectorOnly;

    @Override
    protected DbListOperation mkOperation() {
        return new DbListOperation(dbGateway, new DbListRequest($vectorOnly));
    }

    @Override
    protected final OutputAll execute(List<Database> result) {
        val data = result.stream()
            .map((db) -> Map.of(
                "Name", name(db),
                "id", id(db),
                "Regions", regions(db),
                "Cloud", cloud(db),
                "V", vector(db),
                "Status", status(db)
            ))
            .toList();

        return new ShellTable(data).withColumns("Name", "id", "Regions", "Cloud", "V", "Status");
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
        return db.getInfo().getDbType() != null ? "■" : "";
    }

    private String status(Database db) {
        return AstraColors.highlight(db.getStatus());
    }
}
