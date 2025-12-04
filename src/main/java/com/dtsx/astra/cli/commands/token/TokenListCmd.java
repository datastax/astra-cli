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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.dtsx.astra.cli.operations.token.TokenListOperation.TokenInfo;
import static com.dtsx.astra.cli.utils.CollectionUtils.sequencedMapOf;

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
        return OutputJson.serializeValue(getSorted(tokens).map((t) -> sequencedMapOf(
            "generatedOn", t.raw().getGeneratedOn(),
            "clientId", t.raw().getClientId(),
            "tokenExpiry", t.raw().getTokenExpiry(),
            "description", t.raw().getDescription(),
            "roleNames", t.roleNames(),
            "roleIds", t.roleIds()
        )).toList());
    }

    @Override
    public final OutputAll execute(Supplier<Stream<TokenInfo>> tokens) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

        val rows = getSorted(tokens)
            .map((token) -> sequencedMapOf(
                "Generated On", formatter.format(Instant.parse(token.raw().getGeneratedOn())),
                "Client Id", token.raw().getClientId(),
                "Roles", token.roleNames()
            ))
            .toList();

        return new ShellTable(rows).withColumns("Generated On", "Client Id", "Roles");
    }

    private Stream<TokenInfo> getSorted(Supplier<Stream<TokenInfo>> tokens) {
        return tokens.get().sorted(Comparator.<TokenInfo, String>comparing(t -> t.raw().getGeneratedOn()).reversed());
    }

    @Override
    protected Operation<Stream<TokenInfo>> mkOperation() {
        val roleGateway = ctx.gateways().mkRoleGateway(profile().token(), profile().env(), new RoleCompletionsCache(ctx));
        return new TokenListOperation(tokenGateway, roleGateway);
    }
}
