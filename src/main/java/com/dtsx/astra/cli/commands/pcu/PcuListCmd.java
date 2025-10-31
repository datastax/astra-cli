package com.dtsx.astra.cli.commands.pcu;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "list",
    description = "List your PCU groups"
)
@Example(
    comment = "List all your PCU groups",
    command = "${cli.name} pcu list"
)
public final class PcuListCmd extends PcuListImpl {}
