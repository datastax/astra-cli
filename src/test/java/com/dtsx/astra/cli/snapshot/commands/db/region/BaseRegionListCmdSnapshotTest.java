package com.dtsx.astra.cli.snapshot.commands.db.region;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway.RegionInfo;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;

import java.util.SortedMap;
import java.util.TreeMap;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public abstract class BaseRegionListCmdSnapshotTest extends BaseCmdSnapshotTest {
    private static final SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>> FOUND_REGIONS = new TreeMap<>() {{
        put(CloudProviderType.AWS, new TreeMap<>() {{
            put("us-east-1", new RegionInfo("US East (N. Virginia)", false, "North America", "*fake_raw_region (us-east-1)*"));
            put("us-west-2", new RegionInfo("US West (Oregon)", true, "North America", "*fake_raw_region (us-west-2)*"));
        }});

        put(CloudProviderType.GCP, new TreeMap<>() {{
            put("us-central1", new RegionInfo("Iowa", false, "North America", "*fake_raw_region (us-central1)*"));
            put("us-east1", new RegionInfo("South Carolina", true, "Europe", "*fake_raw_region (us-east1)*"));
        }});

        put(CloudProviderType.AZURE, new TreeMap<>() {{
            put("eastus", new RegionInfo("East US", false, "North America", "*fake_raw_region (eastus)*"));
            put("westus2", new RegionInfo("West US 2", true, "Europe", "*fake_raw_region (westus2)*"));
        }});
    }};

    private SnapshotTestOptionsModifier mkOpts(SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>> ret) {
        return (o) -> o
            .gateway(RegionGateway.class, (mock) -> {
                doReturn(ret).when(mock).findAllServerless(anyBoolean());
                doReturn(ret).when(mock).findAllClassic();
            })
            .verify((mocks) -> {
                verifyMockGateway(mocks.regionGateway());
            });
    }

    protected abstract String getCommandName();
    protected abstract void verifyMockGateway(RegionGateway mock);

    @TestForAllOutputs
    public void regions_found(OutputType outputType) {
        verifyRun("db " + getCommandName(), outputType, mkOpts(FOUND_REGIONS));
    }

    @TestForDifferentOutputs
    public void regions_found_with_name_filter(OutputType outputType) {
        verifyRun("db " + getCommandName() + " --filter east", outputType, mkOpts(FOUND_REGIONS));
    }

    @TestForDifferentOutputs
    public void regions_found_with_cloud_filter(OutputType outputType) {
        verifyRun("db " + getCommandName() + " -c gcp,aws", outputType, mkOpts(FOUND_REGIONS));
    }

    @TestForDifferentOutputs
    public void regions_found_with_zone_filter(OutputType outputType) {
        verifyRun("db " + getCommandName() + " --zone Europe", outputType, mkOpts(FOUND_REGIONS));
    }

    @TestForDifferentOutputs
    public void no_regions_found(OutputType outputType) {
        verifyRun("db " + getCommandName(), outputType, mkOpts(new TreeMap<>()));
    }
}
