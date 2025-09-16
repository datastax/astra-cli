package com.dtsx.astra.cli.snapshot.commands.org;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrgNameCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier orgOpts = (o) -> o
        .gateway(OrgGateway.class, (mock) -> {
            when(mock.current()).thenReturn(Fixtures.Organization);
        })
        .verify((mocks) -> {
            verify(mocks.orgGateway()).current();
        });

    @TestForDifferentOutputs
    public void org_name(OutputType outputType) {
        verifyRun("org name", outputType, orgOpts);
    }
}
