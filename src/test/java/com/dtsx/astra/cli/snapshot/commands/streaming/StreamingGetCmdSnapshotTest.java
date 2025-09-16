package com.dtsx.astra.cli.snapshot.commands.streaming;

import com.dtsx.astra.cli.core.exceptions.internal.streaming.role.TenantNotFoundException;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Tenants;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StreamingGetCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier foundStreamingOpts = (o) -> o
        .gateway(StreamingGateway.class, (mock) -> {
            when(mock.findOne(any())).thenReturn(Tenants.One);
        })
        .verify((mocks) -> {
            verify(mocks.streamingGateway()).findOne(Tenants.Name);
        });

    private final SnapshotTestOptionsModifier notFoundStreamingOpts = (o) -> o
        .gateway(StreamingGateway.class, (mock) -> {
            when(mock.findOne(any())).thenThrow(new TenantNotFoundException(TenantName.mkUnsafe("*nonexistent*")));
        })
        .verify((mocks) -> {
            verify(mocks.streamingGateway()).findOne(TenantName.mkUnsafe("*nonexistent*"));
        });

    @TestForAllOutputs
    public void streaming_full_info(OutputType outputType) {
        verifyRun("streaming get ${TenantName}", outputType, foundStreamingOpts);
    }

    @TestForAllOutputs
    public void streaming_partial_info(OutputType outputType) {
        verifyRun("streaming get ${TenantName} --key status", outputType, foundStreamingOpts);
    }

    @TestForAllOutputs
    public void streaming_not_found(OutputType outputType) {
        verifyRun("streaming get *nonexistent*", outputType, notFoundStreamingOpts);
    }
}
