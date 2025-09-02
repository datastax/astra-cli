package com.dtsx.astra.cli.snapshot.commands.token;

import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Roles;
import com.dtsx.astra.cli.testlib.Fixtures.Tokens;
import com.dtsx.astra.sdk.org.domain.IamToken;
import lombok.val;

import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TokenListCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier mkOpts(Stream<IamToken> ret) {
        return (o) -> o
            .gateway(TokenGateway.class, (mock) -> {
                when(mock.findAll()).thenReturn(ret);
            })
            .gateway(RoleGateway.class, (mock) -> {
                for (val info : Tokens.Infos) {
                    val tokenRoleId = getTokenRoleRef(info);

                    val matchingRole = Roles.Many.stream()
                        .filter(r -> r.getId().equals(tokenRoleId.toString()))
                        .findFirst();

                    when(mock.tryFindOne(tokenRoleId)).thenReturn(matchingRole);
                }
            })
            .verify((mocks) -> {
                verify(mocks.tokenGateway()).findAll();
            });
    }

    @TestForAllOutputs
    public void tokens_found(OutputType outputType) {
        verifyRun("token list", outputType, o -> o.use(mkOpts(Tokens.Infos.stream()))
            .verify((mocks) -> {
                for (val info : Tokens.Infos) {
                    verify(mocks.roleGateway()).tryFindOne(getTokenRoleRef(info));
                }
            }));
    }

    @TestForDifferentOutputs
    public void no_tokens_found(OutputType outputType) {
        verifyRun("token list", outputType, o -> o.use(mkOpts(Stream.of()))
            .verify((mocks) -> {
                verify(mocks.roleGateway(), never()).tryFindOne(any());
            }));
    }

    private RoleRef getTokenRoleRef(IamToken token) {
        return RoleRef.fromId(UUID.fromString(token.getRoles().getFirst()));
    }
}
