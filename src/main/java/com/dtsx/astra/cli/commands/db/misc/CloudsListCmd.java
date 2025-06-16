package com.dtsx.astra.cli.commands.db.misc;

import com.dtsx.astra.cli.commands.db.region.AbstractRegionCmd;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.misc.CloudsListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.Map;
import java.util.TreeSet;

@Command(
    name = "list-clouds"
)
public final class CloudsListCmd extends AbstractRegionCmd {
    @Override
    protected OutputAll execute() {
        val set = new TreeSet<>(new CloudsListOperation(regionGateway).execute());

        return new ShellTable(
            set.stream()
                .map(c -> Map.of("Cloud Provider", c.toLowerCase()))
                .toList()
        ).withColumns("Cloud Provider");
    }
}
