package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "list",
    description = "List your non-terminated Astra databases."
)
@Example(
    comment = "List all your Astra databases",
    command = "${cli.name} db list"
)
@Example(
    comment = "List only vector-enabled Astra databases",
    command = "${cli.name} db list --vector"
)
public final class DbListCmd extends DbListImpl {}
