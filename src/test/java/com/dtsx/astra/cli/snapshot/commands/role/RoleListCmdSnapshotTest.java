package com.dtsx.astra.cli.snapshot.commands.role;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Roles;
import com.dtsx.astra.sdk.org.domain.Role;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RoleListCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier mkOpts(Stream<Role> ret) {
        return (o) -> o
            .gateway(RoleGateway.class, (mock) -> {
                when(mock.findAll()).thenReturn(ret);
            })
            .verify((mocks) -> {
                verify(mocks.roleGateway()).findAll();
            });
    }

    @TestForAllOutputs
    public void roles_found(OutputType outputType) {
        verifyRun("role list", outputType, mkOpts(Roles.Many.stream()));
    }

    @TestForDifferentOutputs
    public void no_roles_found(OutputType outputType) {
        verifyRun("role list", outputType, mkOpts(Stream.of()));
    }
}
