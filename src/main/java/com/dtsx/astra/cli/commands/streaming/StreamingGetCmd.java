package com.dtsx.astra.cli.commands.streaming;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.RenderableShellTable;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.StreamingGetOperation;
import com.dtsx.astra.cli.operations.streaming.StreamingGetOperation.StreamingGetRequest;
import com.dtsx.astra.cli.operations.streaming.StreamingGetOperation.StreamingInfo;
import com.dtsx.astra.cli.operations.streaming.StreamingGetOperation.StreamingInfoFull;
import com.dtsx.astra.cli.operations.streaming.StreamingGetOperation.StreamingInfoValue;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.LinkedHashMap;
import java.util.Optional;

@Command(
    name = "get",
    aliases = "describe",
    description = "Get the info about a streaming tenant."
)
@Example(
    comment = "Get information about a specific tenant",
    command = "astra streaming get my_tenant"
)
@Example(
    comment = "Get a specific attribute of a tenant",
    command = "astra streaming get my_tenant --key region"
)
public class StreamingGetCmd extends AbstractStreamingTenantSpecificCmd<StreamingInfo> {
    public enum StreamingGetKeys {
        status,
        cloud,
        pulsar_token,
        region,
    }

    @Option(
        names = { "-k", "--key" },
        description = "Specific tenant attribute to retrieve",
        paramLabel = "KEY"
    )
    public Optional<StreamingGetKeys> $key;

    @Override
    protected final OutputAll executeJson(StreamingInfo result) {
        return switch (result) {
            case StreamingInfoFull info -> OutputAll.serializeValue(info.raw());
            case StreamingInfoValue(var value) -> OutputAll.serializeValue(value);
        };
    }

    @Override
    protected final OutputAll execute(StreamingInfo result) {
        return switch (result) {
            case StreamingInfoFull info -> mkShellTable(info);
            case StreamingInfoValue(var value) -> OutputAll.serializeValue(value);
        };
    }

    private RenderableShellTable mkShellTable(StreamingInfoFull info) {
        return ShellTable.forAttributes(new LinkedHashMap<>() {{
            put("Name", $tenantName.unwrap());
            put("Status", info.status().unwrap());
            put("Cloud Provider", info.cloud().name().toLowerCase());
            put("Cloud Region", info.region().unwrap());
            put("Cluster Name", info.clusterName());
            put("Pulsar Version", info.pulsarVersion());
            put("Jvm Version", info.jvmVersion());
            put("WebServiceUrl", info.webServiceUrl());
            put("BrokerServiceUrl", info.brokerServiceUrl());
            put("WebSocketUrl", info.webSocketUrl());
        }});
    }

    @Override
    protected Operation<StreamingInfo> mkOperation() {
        return new StreamingGetOperation(streamingGateway, new StreamingGetRequest($tenantName, $key));
    }
}
