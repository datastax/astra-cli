package com.dtsx.astra.cli.snapshot.commands.user;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.user.UserGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Roles;
import com.dtsx.astra.cli.testlib.Fixtures.Users;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class UserInviteCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier userInvitedOpts = (o) -> o
        .gateway(UserGateway.class, (mock) -> {
            doReturn(CreationStatus.created(List.of(Roles.One.getId()))).when(mock).invite(any(), any());
        })
        .verify((mocks) -> {
            verify(mocks.userGateway()).invite(Users.EmailRef, List.of(Roles.NameRef));
        });

    private final SnapshotTestOptionsModifier userAlreadyExistsOpts = (o) -> o
        .gateway(UserGateway.class, (mock) -> {
            doReturn(CreationStatus.alreadyExists(null)).when(mock).invite(any(), any());
        })
        .verify((mocks) -> {
            verify(mocks.userGateway()).invite(any(), any());
        });

    @TestForAllOutputs
    public void invite_user(OutputType outputType) {
        verifyRun("user invite ${EmailRef} --roles ${RoleName}", outputType, userInvitedOpts);
    }

    @TestForAllOutputs
    public void allow_user_already_invited(OutputType outputType) {
        verifyRun("user invite ${EmailRef} --roles ${RoleName} --if-not-exists", outputType, userAlreadyExistsOpts);
    }

    @TestForAllOutputs
    public void error_user_already_invited(OutputType outputType) {
        verifyRun("user invite ${EmailRef} --roles ${RoleName}", outputType, userAlreadyExistsOpts);
    }
}
