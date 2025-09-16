package com.dtsx.astra.cli.snapshot.commands.user;

import com.dtsx.astra.cli.core.exceptions.internal.user.UserNotFoundException;
import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.user.UserGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Users;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserGetCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier foundUserOpts = (o) -> o
        .gateway(UserGateway.class, (mock) -> {
            when(mock.findOne(any())).thenReturn(Users.One);
        })
        .verify((mocks) -> {
            verify(mocks.userGateway()).findOne(Users.EmailRef);
        });

    private final SnapshotTestOptionsModifier notFoundUserOpts = (o) -> o
        .gateway(UserGateway.class, (mock) -> {
            when(mock.findOne(any())).thenThrow(new UserNotFoundException(UserRef.fromEmailUnsafe("nonexistent@example.com")));
        })
        .verify((mocks) -> {
            verify(mocks.userGateway()).findOne(UserRef.fromEmailUnsafe("nonexistent@example.com"));
        });

    @TestForAllOutputs
    public void user_full_info(OutputType outputType) {
        verifyRun("user get ${EmailRef}", outputType, foundUserOpts);
    }

    @TestForAllOutputs
    public void user_not_found(OutputType outputType) {
        verifyRun("user get nonexistent@example.com", outputType, notFoundUserOpts);
    }
}
