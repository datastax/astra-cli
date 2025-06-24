package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.token.TokenListOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.Map;

@Command(name = "list", description = "Display the list of tokens in an organization")
public final class TokenListCmd extends AbstractTokenCmd {
    @Override
    public OutputAll execute() {
        val operation = new TokenListOperation(tokenGateway, roleGateway);
        val tokens = operation.execute();
        
        val rows = tokens.stream()
            .map(token -> Map.of(
                "Generated On", token.generatedOn(),
                "Client Id", token.clientId(),
                "Role", token.role()
            ))
            .toList();
        
        return new ShellTable(rows).withColumns("Generated On", "Client Id", "Role");
    }
}
