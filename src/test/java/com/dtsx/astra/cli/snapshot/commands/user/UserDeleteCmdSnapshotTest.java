package com.dtsx.astra.cli.snapshot.commands.user;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.user.UserGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Users;
import com.dtsx.astra.cli.core.models.UserRef;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserDeleteCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier userDeletedOpts = (o) -> o
        .gateway(UserGateway.class, (mock) -> {
            doReturn(DeletionStatus.deleted(null)).when(mock).delete(any());
        })
        .verify((mocks) -> {
            verify(mocks.userGateway()).delete(Users.EmailRef);
        });

    private final SnapshotTestOptionsModifier userNotFoundOpts = (o) -> o
        .gateway(UserGateway.class, (mock) -> {
            doReturn(DeletionStatus.notFound(null)).when(mock).delete(any());
        })
        .verify((mocks) -> {
            verify(mocks.userGateway()).delete(UserRef.fromEmailUnsafe("nonexistent@example.com"));
        });

    @TestForAllOutputs
    public void delete_user(OutputType outputType) {
        verifyRun("user delete ${EmailRef}", outputType, userDeletedOpts);
    }

    @TestForDifferentOutputs
    public void allow_user_not_found(OutputType outputType) {
        verifyRun("user delete nonexistent@example.com --if-exists", outputType, userNotFoundOpts);
    }

    @TestForAllOutputs
    public void error_user_not_found(OutputType outputType) {
        verifyRun("user delete nonexistent@example.com", outputType, userNotFoundOpts);
    }
}
