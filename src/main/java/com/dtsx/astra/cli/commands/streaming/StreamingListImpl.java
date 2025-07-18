package com.dtsx.astra.cli.commands.streaming;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.streaming.StreamingListOperation;
import com.dtsx.astra.cli.operations.streaming.StreamingListOperation.TenantInfo;
import lombok.val;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class StreamingListImpl extends AbstractStreamingCmd<Stream<TenantInfo>> {
    @Override
    protected OutputJson executeJson(Supplier<Stream<TenantInfo>> result) {
        return OutputJson.serializeValue(result.get().map(TenantInfo::raw).toList());
    }

    @Override
    protected final OutputAll execute(Supplier<Stream<TenantInfo>> result) {
        val data = result.get()
            .map((tenant) -> Map.of(
                "name", tenant.name(),
                "cloud", tenant.cloud().name(),
                "region", tenant.region().unwrap(),
                "status", tenant.status().highlight()
            ))
            .toList();

        return new ShellTable(data).withColumns("name", "cloud", "region", "status");
    }

    @Override
    protected Operation<Stream<TenantInfo>> mkOperation() {
        return new StreamingListOperation(streamingGateway);
    }
}
