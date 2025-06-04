package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.output.AstraColors;
import com.dtsx.astra.cli.output.output.OutputAll;
import com.dtsx.astra.cli.output.table.ShellTable;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Map;

public class DbListImpl extends AbstractDbCmd {
    @Option(names = "--vector", description = "Only show vector-enabled databases")
    protected boolean vectorOnly;

    @NotNull
    @Override
    public OutputAll execute() {
        val data = dbService.findDatabases().stream()
            .filter(this::filterDbType)
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

    private boolean filterDbType(Database db) {
        if (vectorOnly) {
            return db.getInfo().getDbType() != null;
        }
        return true;
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
        return AstraColors.colorStatus(db.getStatus());
    }
}
