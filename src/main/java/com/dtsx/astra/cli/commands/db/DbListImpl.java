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

public class DbListImpl extends AbstractDbCmd {
    @Option(names = "--vector", description = "Only show vector-enabled databases")
    protected boolean vectorOnly;

    private DbListOperation dbListOperation;

    @Override
    protected void prelude() {
        super.prelude();
        this.dbListOperation = new DbListOperation(dbGateway);
    }

    @Override
    public OutputAll execute() {
        val databases = dbListOperation.execute(vectorOnly);

        val data = databases.stream()
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
