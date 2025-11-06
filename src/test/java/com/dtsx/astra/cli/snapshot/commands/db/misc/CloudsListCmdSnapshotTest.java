package com.dtsx.astra.cli.snapshot.commands.db.misc;

import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;

import java.util.SortedSet;
import java.util.TreeSet;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CloudsListCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier mkOpts(SortedSet<CloudProvider> ret) {
        return (o) -> o
            .gateway(RegionGateway.class, (mock) -> {
                when(mock.findAvailableClouds()).thenReturn(ret);
            })
            .verify((mocks) -> {
                verify(mocks.regionGateway()).findAvailableClouds();
            });
    }

    @TestForAllOutputs
    public void clouds_found(OutputType outputType) {
        verifyRun("db list-clouds", outputType, mkOpts(new TreeSet<>() {{
            add(CloudProvider.AWS);
            add(CloudProvider.GCP);
            add(CloudProvider.AZURE);
        }}));
    }

    @TestForDifferentOutputs
    public void no_clouds_found(OutputType outputType) {
        verifyRun("db list-clouds", outputType, mkOpts(new TreeSet<>()));
    }
}
