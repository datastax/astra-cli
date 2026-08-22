package com.dtsx.astra.cli.snapshot.commands.org;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrgIdCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier orgOpts = (o) -> o
        .gateway(OrgGateway.class, (mock) -> {
            when(mock.find(any(), any())).thenReturn(Fixtures.Organization);
        })
        .verify((mocks) -> {
            verify(mocks.orgGateway()).find(Fixtures.Token, Fixtures.Profile.env());
        });

    @TestForDifferentOutputs
    public void org_id(OutputType outputType) {
        verifyRun("org id", outputType, orgOpts);
    }
}
