package com.dtsx.astra.cli.snapshot.commands.streaming;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Tenants;
import com.dtsx.astra.cli.utils.JsonUtils;
import com.dtsx.astra.sdk.streaming.domain.Tenant;
import lombok.val;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StreamingStatusCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier mkOpts(Tenant ret) {
        return (o) -> o
            .gateway(StreamingGateway.class, (mock) -> {
                when(mock.findOne(Tenants.Name)).thenReturn(ret);
            })
            .verify((mocks) -> {
                verify(mocks.streamingGateway()).findOne(Tenants.Name);
            });
    }

    @TestForAllOutputs
    public void streaming_status_found(OutputType outputType) {
        verifyRun("streaming status ${TenantName}", outputType, mkOpts(Tenants.One));
    }

    @TestForAllOutputs
    public void error_streaming_not_found(OutputType outputType) {
        val clone = JsonUtils.clone(Tenants.One, Tenant.class);
        clone.setStatus("i_like_cars");
        verifyRun("streaming status ${TenantName}", outputType, mkOpts(clone));
    }
}
