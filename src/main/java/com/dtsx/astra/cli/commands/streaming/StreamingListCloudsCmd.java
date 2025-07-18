package com.dtsx.astra.cli.commands.streaming;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.StreamingListCloudsOperation;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Command(
    name = "list-clouds",
    description = "List all available cloud providers for Astra streaming tenants"
)
@Example(
    comment = "List all available cloud providers for streaming tenants",
    command = "astra streaming list-clouds"
)
public class StreamingListCloudsCmd extends AbstractStreamingCmd<Set<CloudProviderType>> {
    @Override
    protected OutputJson executeJson(Supplier<Set<CloudProviderType>> clouds) {
        val data = clouds.get()
            .stream()
            .map((cloud) -> Map.of("cloudProvider", cloud.name()))
            .toList();

        return OutputJson.serializeValue(data);
    }

    @Override
    protected OutputAll execute(Supplier<Set<CloudProviderType>> clouds) {
        val data = clouds.get()
            .stream()
            .map((cloud) -> Map.of("Cloud Provider", cloud.name().toLowerCase()))
            .toList();

        return new ShellTable(data).withColumns("Cloud Provider");
    }

    @Override
    protected Operation<Set<CloudProviderType>> mkOperation() {
        return new StreamingListCloudsOperation(streamingGateway);
    }
}
