package com.dtsx.astra.cli.commands.org;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "get",
    description = "Show organization information"
)
@Example(
    comment = "Show organization information",
    command = "${cli.name} org get"
)
public class OrgGetCmd extends OrgGetImpl {}
