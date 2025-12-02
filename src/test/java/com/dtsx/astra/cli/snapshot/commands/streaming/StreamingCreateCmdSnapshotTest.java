package com.dtsx.astra.cli.snapshot.commands.streaming;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Regions;
import com.dtsx.astra.cli.testlib.Fixtures.Tenants;
import com.dtsx.astra.sdk.streaming.domain.Tenant;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class StreamingCreateCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier fromCloud(Function<Tenant, CreationStatus<Tenant>> lift) {
        return (o) -> o
            .gateway(StreamingGateway.class, (mock) -> {
                when(mock.findCloudForRegion(any(), any())).thenReturn(CloudProvider.AWS);

                doReturn(lift.apply(Tenants.One)).when(mock).create(any(), any(), any(), any());
            })
            .verify((mocks) -> {
                verify(mocks.streamingGateway()).findCloudForRegion(Optional.of(CloudProvider.AWS), Regions.NAME);

                verify(mocks.streamingGateway()).create(Tenants.Name, Either.pure(Pair.of(CloudProvider.AWS, Regions.NAME)), "serverless", null);
            });
    }

    private SnapshotTestOptionsModifier fromCluster(Function<Tenant, CreationStatus<Tenant>> lift) {
        return (o) -> o
            .gateway(StreamingGateway.class, (mock) -> {
                doReturn(lift.apply(Tenants.One)).when(mock).create(any(), any(), any(), any());
            })
            .verify((mocks) -> {
                verify(mocks.streamingGateway(), never()).findCloudForRegion(any(), any());

                verify(mocks.streamingGateway()).create(Tenants.Name, Either.left("*cluster*"), "serverless", null);
            });
    }
    
    @TestForAllOutputs
    public void streaming_create_from_cloud(OutputType outputType) {
        verifyRun("streaming create ${TenantName} --region ${RegionName} --cloud AWS", outputType, fromCloud(CreationStatus::created));
    }

    @TestForAllOutputs
    public void streaming_create_from_cluster(OutputType outputType) {
        verifyRun("streaming create ${TenantName} --cluster *cluster*", outputType, fromCluster(CreationStatus::created));
    }

    @TestForDifferentOutputs
    public void error_streaming_already_exists(OutputType outputType) {
        verifyRun("streaming create ${TenantName} --region ${RegionName} --cloud AWS", outputType, fromCloud(CreationStatus::alreadyExists));
    }

    @TestForDifferentOutputs
    public void allow_streaming_already_exists(OutputType outputType) {
        verifyRun("streaming create ${TenantName} --cluster *cluster* --if-not-exists", outputType, fromCluster(CreationStatus::alreadyExists));
    }
}
