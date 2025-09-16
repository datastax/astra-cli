package com.dtsx.astra.cli.snapshot.commands.token;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class TokenDeleteCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier tokenDeletedOpts = (o) -> o
        .gateway(TokenGateway.class, (mock) -> {
            doReturn(DeletionStatus.deleted(null)).when(mock).delete(any());
        })
        .verify((mocks) -> {
            verify(mocks.tokenGateway()).delete(Fixtures.CreateTokenResponse.getClientId());
        });

    private final SnapshotTestOptionsModifier tokenNotFoundOpts = (o) -> o
        .gateway(TokenGateway.class, (mock) -> {
            doReturn(DeletionStatus.notFound(null)).when(mock).delete(any());
        })
        .verify((mocks) -> {
            verify(mocks.tokenGateway()).delete("*nonexistent*");
        });

    @TestForAllOutputs
    public void delete_token(OutputType outputType) {
        verifyRun("token delete ${TokenClientId}", outputType, tokenDeletedOpts);
    }

    @TestForDifferentOutputs
    public void allow_token_not_found(OutputType outputType) {
        verifyRun("token delete *nonexistent* --if-exists", outputType, tokenNotFoundOpts);
    }

    @TestForAllOutputs
    public void error_token_not_found(OutputType outputType) {
        verifyRun("token delete *nonexistent*", outputType, tokenNotFoundOpts);
    }
}
