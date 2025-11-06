package com.dtsx.astra.cli.commands.org;

import com.dtsx.astra.cli.core.docs.AliasForSubcommand;
import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "org",
    description = {
        "Get organization information",
        "",
        "The @|code --token|@ flag is handy to quickly get information about another organization.",
        "",
        "Use the @|code --token @file|@ syntax to read the token from a file, to avoid potential leaks.",
    },
    subcommands = {
        OrgGetCmd.class,
        OrgIdCmd.class,
        OrgNameCmd.class,
    }
)
@Example(
    comment = "Get your organization's information",
    command = "${cli.name} org"
)
@Example(
    comment = "Get information about another organization",
    command = "${cli.name} org get --token AstraCS:..."
)
@AliasForSubcommand(OrgGetCmd.class)
public final class OrgCmd extends OrgGetImpl {}
