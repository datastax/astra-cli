package com.dtsx.astra.cli.commands.db.misc;

import com.dtsx.astra.cli.commands.db.region.AbstractRegionCmd;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.misc.CloudsListOperation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

@Command(
    name = "list-clouds",
    description = "List all cloud providers with regions available for Astra DB"
)
@Example(
    comment = "List all available cloud providers",
    command = "${cli.name} db list-clouds"
)
public class CloudsListCmd extends AbstractRegionCmd<SortedSet<CloudProviderType>> {
    @Override
    protected Operation<SortedSet<CloudProviderType>> mkOperation() {
        return new CloudsListOperation(regionGateway);
    }

    @Override
    protected final OutputAll execute(Supplier<SortedSet<CloudProviderType>> result) {
        val set = new TreeSet<>(result.get());

        return new ShellTable(
            set.stream()
                .map(c -> sequencedMapOf("Cloud Provider", c.name().toLowerCase()))
                .toList()
        ).withColumns("Cloud Provider");
    }
}
