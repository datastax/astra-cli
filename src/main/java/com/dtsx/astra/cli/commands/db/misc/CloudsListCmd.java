package com.dtsx.astra.cli.commands.db.misc;

import com.dtsx.astra.cli.commands.db.region.AbstractRegionCmd;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.misc.CloudsListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.dtsx.astra.cli.operations.db.misc.CloudsListOperation.*;

@Command(
    name = "list-clouds"
)
public class CloudsListCmd extends AbstractRegionCmd<Set<String>> {
    @Override
    protected CloudsListOperation mkOperation() {
        return new CloudsListOperation(regionGateway, new CloudsListRequest());
    }

    @Override
    protected final OutputAll execute(Set<String> result) {
        val set = new TreeSet<>(result);

        return new ShellTable(
            set.stream()
                .map(c -> Map.of("Cloud Provider", c.toLowerCase()))
                .toList()
        ).withColumns("Cloud Provider");
    }
}
