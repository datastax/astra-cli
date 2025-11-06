package com.dtsx.astra.cli.snapshot.commands.streaming;

import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway.StreamingRegionInfo;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class StreamingListRegionsCmdSnapshotTest extends BaseCmdSnapshotTest {
    private static final SortedMap<CloudProvider, ? extends SortedMap<String, StreamingRegionInfo>> FOUND_REGIONS = new TreeMap<>() {{
        put(CloudProvider.AWS, new TreeMap<>() {{
            put("us-east-1", new StreamingRegionInfo("US East (N. Virginia)", false, Map.of("raw", "info:us-east-1")));
            put("us-west-2", new StreamingRegionInfo("US West (Oregon)", true, Map.of("raw", "info:us-west-2")));
        }});

        put(CloudProvider.GCP, new TreeMap<>() {{
            put("us-central1", new StreamingRegionInfo("Iowa", false, Map.of("raw", "info:us-central1")));
            put("us-east1", new StreamingRegionInfo("South Carolina", true, Map.of("raw", "info:us-east1")));
        }});

        put(CloudProvider.AZURE, new TreeMap<>() {{
            put("eastus", new StreamingRegionInfo("East US", false, Map.of("raw", "info:eastus")));
            put("westus2", new StreamingRegionInfo("West US 2", true, Map.of("raw", "info:westus2")));
        }});
    }};

    private SnapshotTestOptionsModifier mkOpts(SortedMap<CloudProvider, ? extends SortedMap<String, StreamingRegionInfo>> ret) {
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
