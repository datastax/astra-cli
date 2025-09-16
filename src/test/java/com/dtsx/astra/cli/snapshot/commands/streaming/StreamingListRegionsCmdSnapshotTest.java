package com.dtsx.astra.cli.snapshot.commands.streaming;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway.StreamingRegionInfo;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;

import java.util.SortedMap;
import java.util.TreeMap;

import static org.mockito.Mockito.*;

public class StreamingListRegionsCmdSnapshotTest extends BaseCmdSnapshotTest {
    private static final SortedMap<CloudProviderType, ? extends SortedMap<String, StreamingRegionInfo>> FOUND_REGIONS = new TreeMap<>() {{
        put(CloudProviderType.AWS, new TreeMap<>() {{
            put("us-east-1", new StreamingRegionInfo("US East (N. Virginia)", false, null));
            put("us-west-2", new StreamingRegionInfo("US West (Oregon)", true, null));
        }});

        put(CloudProviderType.GCP, new TreeMap<>() {{
            put("us-central1", new StreamingRegionInfo("Iowa", false, null));
            put("us-east1", new StreamingRegionInfo("South Carolina", true, null));
        }});

        put(CloudProviderType.AZURE, new TreeMap<>() {{
            put("eastus", new StreamingRegionInfo("East US", false, null));
            put("westus2", new StreamingRegionInfo("West US 2", true, null));
        }});
    }};

    private SnapshotTestOptionsModifier mkOpts(SortedMap<CloudProviderType, ? extends SortedMap<String, StreamingRegionInfo>> ret) {
        return (o) -> o
            .gateway(StreamingGateway.class, (mock) -> {
                doReturn(ret).when(mock).findAllRegions();
            })
            .verify((mocks) -> {
                verify(mocks.streamingGateway()).findAllRegions();
            });
    }

    @TestForAllOutputs
    public void regions_found(OutputType outputType) {
        verifyRun("streaming list-regions", outputType, mkOpts(FOUND_REGIONS));
    }

    @TestForDifferentOutputs
    public void regions_found_with_name_filter(OutputType outputType) {
        verifyRun("streaming list-regions --filter east", outputType, mkOpts(FOUND_REGIONS));
    }

    @TestForDifferentOutputs
    public void regions_found_with_cloud_filter(OutputType outputType) {
        verifyRun("streaming list-regions --cloud gcp", outputType, mkOpts(FOUND_REGIONS));
    }

    @TestForDifferentOutputs
    public void no_regions_found(OutputType outputType) {
        verifyRun("streaming list-regions", outputType, mkOpts(new TreeMap<>()));
    }
}
