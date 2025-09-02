package com.dtsx.astra.cli.commands.streaming;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.StreamingListCloudsOperation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.SortedSet;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;

@Command(
    name = "list-clouds",
    description = "List all available cloud providers for Astra streaming tenants"
)
@Example(
    comment = "List all available cloud providers for streaming tenants",
    command = "${cli.name} streaming list-clouds"
)
public class StreamingListCloudsCmd extends AbstractStreamingCmd<SortedSet<CloudProviderType>> {
    @Override
    protected final OutputJson executeJson(Supplier<SortedSet<CloudProviderType>> clouds) {
        val data = clouds.get()
            .stream()
            .map((cloud) -> sequencedMapOf("cloudProvider", cloud.name()))
            .toList();

        return OutputJson.serializeValue(data);
    }

    @Override
    protected final OutputAll execute(Supplier<SortedSet<CloudProviderType>> clouds) {
        val data = clouds.get()
            .stream()
            .map((cloud) -> sequencedMapOf("Cloud Provider", cloud.name().toLowerCase()))
            .toList();

        return new ShellTable(data).withColumns("Cloud Provider");
    }

    @Override
    protected Operation<SortedSet<CloudProviderType>> mkOperation() {
        return new StreamingListCloudsOperation(streamingGateway);
    }
}
