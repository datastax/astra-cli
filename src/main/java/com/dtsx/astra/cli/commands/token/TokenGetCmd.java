package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "get",
    description = "Show your current token"
)
@Example(
    comment = "Show your current token",
    command = "${cli.name} token get"
)

@Example(
    comment = "Copy your current token",
    command = "${cli.name} token get -c"
)
@Example(
    comment = "Show a token from a different profile",
    command = "${cli.name} token get -p <profile>"
)
public class TokenGetCmd extends TokenGetImpl {}
