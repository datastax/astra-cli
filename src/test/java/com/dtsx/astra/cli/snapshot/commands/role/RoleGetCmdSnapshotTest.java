package com.dtsx.astra.cli.snapshot.commands.role;

import com.dtsx.astra.cli.core.exceptions.internal.role.RoleNotFoundException;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Roles;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RoleGetCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier foundRoleOpts = (o) -> o
        .gateway(RoleGateway.class, (mock) -> {
            when(mock.findOne(any())).thenReturn(Roles.One);
        })
        .verify((mocks) -> {
            verify(mocks.roleGateway()).findOne(Roles.NameRef);
        });

    private final SnapshotTestOptionsModifier notFoundRoleOpts = (o) -> o
        .gateway(RoleGateway.class, (mock) -> {
            when(mock.findOne(any())).thenThrow(new RoleNotFoundException(RoleRef.fromNameUnsafe("*nonexistent*")));
        })
        .verify((mocks) -> {
            verify(mocks.roleGateway()).findOne(RoleRef.fromNameUnsafe("*nonexistent*"));
        });

    @TestForAllOutputs
    public void role_full_info(OutputType outputType) {
        verifyRun("role get ${RoleName}", outputType, foundRoleOpts);
    }

    @TestForAllOutputs
    public void role_not_found(OutputType outputType) {
        verifyRun("role get *nonexistent*", outputType, notFoundRoleOpts);
    }
}
