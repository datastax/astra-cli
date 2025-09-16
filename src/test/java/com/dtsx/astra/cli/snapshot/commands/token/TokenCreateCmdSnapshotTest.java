package com.dtsx.astra.cli.snapshot.commands.token;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures;
import com.dtsx.astra.cli.testlib.Fixtures.Roles;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TokenCreateCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier createTokenOpts = (o) -> o
        .gateway(TokenGateway.class, (mock) -> {
            when(mock.create(any(), any())).thenReturn(Fixtures.CreateTokenResponse);
        })
        .verify((mocks) -> {
            verify(mocks.tokenGateway()).create(Roles.NameRef, Optional.of("Test description"));
        });

    @TestForDifferentOutputs
    public void create_token(OutputType outputType) {
        verifyRun("token create --role ${RoleName} --description Test\\ description", outputType, createTokenOpts);
    }
}
