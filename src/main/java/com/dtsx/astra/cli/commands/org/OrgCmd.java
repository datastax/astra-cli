package com.dtsx.astra.cli.commands.org;

import picocli.CommandLine.Command;

@Command(
    name = "org",
    subcommands = {
        OrgGetCmd.class,
        OrgIdCmd.class,
        OrgNameCmd.class
    }
)
public final class OrgCmd extends OrgGetImpl {}
