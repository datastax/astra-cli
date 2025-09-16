package com.dtsx.astra.cli.snapshot.commands.streaming;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Tenants;
import com.dtsx.astra.sdk.streaming.domain.Tenant;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StreamingListCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier mkOpts(Stream<Tenant> ret) {
        return (o) -> o
            .gateway(StreamingGateway.class, (mock) -> {
                when(mock.findAll()).thenReturn(ret);
            })
            .verify((mocks) -> {
                verify(mocks.streamingGateway()).findAll();
            });
    }

    @TestForAllOutputs
    public void tenants_found(OutputType outputType) {
        verifyRun("streaming", outputType, mkOpts(Tenants.Many.stream()));
    }

    @TestForAllOutputs
    public void no_tenants_found(OutputType outputType) {
        verifyRun("streaming list", outputType, mkOpts(Stream.of()));
    }
}
