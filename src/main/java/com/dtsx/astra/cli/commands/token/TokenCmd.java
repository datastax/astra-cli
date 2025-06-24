package com.dtsx.astra.cli.commands.token;

import picocli.CommandLine.Command;

@Command(
    name = "token",
    subcommands = {
        TokenListCmd.class,
        TokenGetCmd.class,
        TokenCreateCmd.class,
        TokenDeleteCmd.class
    }
)
public final class TokenCmd extends TokenGetImpl {}
