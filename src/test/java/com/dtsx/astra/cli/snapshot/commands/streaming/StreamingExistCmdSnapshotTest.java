package com.dtsx.astra.cli.snapshot.commands.streaming;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Tenants;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StreamingExistCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier mkOpts(boolean ret) {
        return (o) -> o
            .gateway(StreamingGateway.class, (mock) -> {
                when(mock.exists(Tenants.Name)).thenReturn(ret);
            })
            .verify((mocks) -> {
                verify(mocks.streamingGateway()).exists(Tenants.Name);
            });
    }

    @TestForDifferentOutputs
    public void tenant_exists(OutputType outputType) {
        verifyRun("streaming exist ${TenantName}", outputType, mkOpts(true));
    }

    @TestForDifferentOutputs
    public void tenant_not_found(OutputType outputType) {
        verifyRun("streaming exist ${TenantName}", outputType, mkOpts(false));
    }
}
