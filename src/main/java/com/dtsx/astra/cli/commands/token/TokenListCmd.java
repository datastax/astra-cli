package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.token.TokenListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.List;
import java.util.Map;

import static com.dtsx.astra.cli.operations.token.TokenListOperation.*;

@Command(name = "list", description = "Display the list of tokens in an organization")
public class TokenListCmd extends AbstractTokenCmd<List<TokenInfo>> {
    @Override
    public final OutputAll execute(List<TokenInfo> tokens) {
        val rows = tokens.stream()
            .map(token -> Map.of(
                "Generated On", token.generatedOn(),
                "Client Id", token.clientId(),
                "Role", token.role()
            ))
            .toList();
        
        return new ShellTable(rows).withColumns("Generated On", "Client Id", "Role");
    }

    @Override
    protected Operation<List<TokenInfo>> mkOperation() {
        return new TokenListOperation(tokenGateway, roleGateway);
    }
}
