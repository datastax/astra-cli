package com.dtsx.astra.cli.snapshot.commands.db.endpoints;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;

public class EndpointsListCmdSnapshotTest extends BaseEndpointCmdSnapshotTest {
    @Override
    @TestForAllOutputs
    public void endpoint_found(OutputType outputType) {
        verifyRun("db endpoints list ${DatabaseId}", outputType, opts);
    }

    @Override
    @TestForDifferentOutputs
    public void endpoint_found_for_region(OutputType outputType) {
        verifyRun("db endpoints list ${DatabaseId} -r ap-south-1", outputType, opts);
    }

    @TestForDifferentOutputs
    public void region_not_found(OutputType outputType) {
        verifyRun("db endpoints list ${DatabaseId} -r i-like-cars-123", outputType, opts);
    }
}
