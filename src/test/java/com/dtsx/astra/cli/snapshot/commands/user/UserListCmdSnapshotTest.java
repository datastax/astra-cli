package com.dtsx.astra.cli.snapshot.commands.user;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.user.UserGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Users;
import com.dtsx.astra.sdk.org.domain.User;

import java.util.stream.Stream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserListCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier mkOpts(Stream<User> ret) {
        return (o) -> o
            .gateway(UserGateway.class, (mock) -> {
                when(mock.findAll()).thenReturn(ret);
            })
            .verify((mocks) -> {
                verify(mocks.userGateway()).findAll();
            });
    }

    @TestForAllOutputs
    public void users_found(OutputType outputType) {
        verifyRun("user list", outputType, mkOpts(Users.Many.stream()));
    }

    @TestForAllOutputs
    public void no_users_found(OutputType outputType) {
        verifyRun("user list", outputType, mkOpts(Stream.of()));
    }
}
