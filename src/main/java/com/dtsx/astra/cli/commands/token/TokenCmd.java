package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.docs.AliasForSubcommand;
import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "token",
    description = "Manage your Astra tokens",
    subcommands = {
        TokenListCmd.class,
        TokenGetCmd.class,
        TokenCreateCmd.class,
        TokenDeleteCmd.class
    }
)
@Example(
    comment = "Show your current token",
    command = "${cli.name} token"
)
@Example(
    comment = "Copy your current token",
    command = "${cli.name} token -c"
)
@Example(
    comment = "Show a token from a different profile",
    command = "${cli.name} token -p <profile>"
)
@AliasForSubcommand(TokenGetCmd.class)
public final class TokenCmd extends TokenGetImpl {}
