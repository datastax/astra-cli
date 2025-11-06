package com.dtsx.astra.cli.snapshot.commands.streaming;

import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StreamingListCloudsCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier opts(SortedSet<CloudProvider> clouds) {
        return (o) -> o
            .gateway(StreamingGateway.class, (mock) -> {
                when(mock.findAvailableClouds()).thenReturn(clouds);
            })
            .verify((mocks) -> {
                verify(mocks.streamingGateway()).findAvailableClouds();
            });
    }

    @TestForAllOutputs
    public void clouds_found(OutputType outputType) {
        verifyRun("streaming list-clouds", outputType, opts(new TreeSet<>() {{
            add(CloudProvider.AWS);
            add(CloudProvider.GCP);
            add(CloudProvider.AZURE);
        }}));
    }

    @TestForDifferentOutputs
    public void no_clouds_found(OutputType outputType) {
        verifyRun("streaming list-clouds", outputType, opts(new TreeSet<>()));
    }
}
