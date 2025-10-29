package com.dtsx.astra.cli.commands.org;

import com.dtsx.astra.cli.core.docs.AliasForSubcommand;
import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "org",
    description = "Show organization information",
    subcommands = {
        OrgGetCmd.class,
        OrgIdCmd.class,
        OrgNameCmd.class,
    }
)
@Example(
    comment = "Show organization information",
    command = "${cli.name} org"
)
@AliasForSubcommand(OrgGetCmd.class)
public final class OrgCmd extends OrgGetImpl {}
