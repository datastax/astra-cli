package com.dtsx.astra.cli.snapshot.commands.org;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.testlib.Fixtures;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class OrgGetCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier orgOpts = (o) -> o
        .gateway(OrgGateway.class, (mock) -> {
            when(mock.current()).thenReturn(Fixtures.Organization);
        })
        .verify((mocks) -> {
            verify(mocks.orgGateway()).current();
        });

    @TestForAllOutputs
    public void org_info(OutputType outputType) {
        verifyRun("org", outputType, orgOpts);
    }
}
