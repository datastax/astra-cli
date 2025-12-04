package com.dtsx.astra.cli.snapshot.commands.token;

import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures;
import com.dtsx.astra.cli.testlib.Fixtures.Roles;
import com.dtsx.astra.sdk.org.domain.IamToken;
import lombok.val;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TokenListCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final Set<UUID> roleIds = Fixtures.TokenInfos.stream()
        .map(info -> getTokenRoleRef(info).toString())
        .map(UUID::fromString)
        .collect(toSet());

    private SnapshotTestOptionsModifier mkOpts(Stream<IamToken> ret) {
        return (o) -> o
            .gateway(TokenGateway.class, (mock) -> {
                when(mock.findAll()).thenReturn(ret);
            })
            .gateway(RoleGateway.class, (mock) -> {
                val roleMappings = Roles.Many.stream()
                    .filter(r -> roleIds.contains(UUID.fromString(r.getId())))
                    .collect(toMap(
                        r -> UUID.fromString(r.getId()),
                        r -> Optional.of(r.getName())
                    ));

                when(mock.findNames(roleIds)).thenReturn(roleMappings);
            })
            .verify((mocks) -> {
                verify(mocks.tokenGateway()).findAll();
            });
    }

    @TestForAllOutputs
    public void tokens_found(OutputType outputType) {
        verifyRun("token list", outputType, o -> o.use(mkOpts(Fixtures.TokenInfos.stream()))
            .verify((mocks) -> {
                verify(mocks.roleGateway()).findNames(roleIds);
            }));
    }

    @TestForDifferentOutputs
    public void no_tokens_found(OutputType outputType) {
        verifyRun("token list", outputType, o -> o.use(mkOpts(Stream.of()))
            .verify((mocks) -> {
                verify(mocks.roleGateway()).findNames(Set.of());
            }));
    }

    private RoleRef getTokenRoleRef(IamToken token) {
        return RoleRef.fromId(UUID.fromString(token.getRoles().getFirst()));
    }
}
