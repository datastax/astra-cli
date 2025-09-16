package com.dtsx.astra.cli.snapshot.commands.streaming;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Tenants;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class StreamingDeleteCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier opts(Function<TenantName, DeletionStatus<TenantName>> lift) {
        return (o) -> o
            .gateway(StreamingGateway.class, (mock) -> {
                doReturn(lift.apply(Tenants.Name)).when(mock).delete(any());
            })
            .verify((mocks) -> {
                verify(mocks.streamingGateway()).delete(Tenants.Name);
            });
    }

    @TestForAllOutputs
    public void streaming_delete(OutputType outputType) {
        verifyRun("streaming delete ${TenantName}", outputType, opts(DeletionStatus::deleted));
    }

    @TestForDifferentOutputs
    public void error_tenant_not_found(OutputType outputType) {
        verifyRun("streaming delete ${TenantName}", outputType, opts(DeletionStatus::notFound));
    }

    @TestForDifferentOutputs
    public void allow_tenant_not_found(OutputType outputType) {
        verifyRun("streaming delete ${TenantName} --if-exists", outputType, opts(DeletionStatus::notFound));
    }
}
