package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.completions.caches.RoleCompletionsCache;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.operations.token.TokenListOperation.TokenInfo;
import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;

@Command(
    name = "list",
    description = "List your tokens' information"
)
@Example(
    comment = "List your tokens' information",
    command = "${cli.name} token list"
)
public class TokenListCmd extends AbstractTokenCmd<Stream<TokenInfo>> {
    @Override
    protected final OutputJson executeJson(Supplier<Stream<TokenInfo>> tokens) {
        return OutputJson.serializeValue(tokens.get().map((t) -> sequencedMapOf(
            "generatedOn", t.generatedOn(),
            "clientId", t.clientId(),
            "roleNames", t.roleNames(),
            "roleIds", t.roleIds()
        )).toList());
    }

    @Override
    public final OutputAll execute(Supplier<Stream<TokenInfo>> tokens) {
        val rows = tokens.get()
            .map((token) -> sequencedMapOf(
                "Generated On", token.generatedOn(),
                "Client Id", token.clientId(),
                "Roles", token.roleNames()
            ))
            .toList();

        return new ShellTable(rows).withColumns("Generated On", "Client Id", "Roles");
    }

    @Override
    protected Operation<Stream<TokenInfo>> mkOperation() {
        val roleGateway = ctx.gateways().mkRoleGateway(profile().token(), profile().env(), new RoleCompletionsCache(ctx), ctx);
        return new TokenListOperation(tokenGateway, roleGateway);
    }
}
