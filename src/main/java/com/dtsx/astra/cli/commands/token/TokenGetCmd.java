package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "get",
    description = "Show your current token"
)
@Example(
    comment = "Show your current token",
    command = "astra token get"
)
public class TokenGetCmd extends TokenGetImpl {}
