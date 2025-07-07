package com.dtsx.astra.cli.commands.org;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "org",
    description = "Show organization information",
    subcommands = {
        OrgGetCmd.class,
        OrgIdCmd.class,
        OrgNameCmd.class
    }
)
@Example(
    comment = "Show organization information",
    command = "astra org"
)
public final class OrgCmd extends OrgGetImpl {}
