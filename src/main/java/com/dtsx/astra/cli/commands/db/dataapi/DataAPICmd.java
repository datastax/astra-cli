package com.dtsx.astra.cli.commands.db.dataapi;

import com.dtsx.astra.cli.commands.user.AbstractCmd;
import com.dtsx.astra.cli.core.docs.AliasForSubcommand;
import com.dtsx.astra.cli.core.docs.AliasForSubcommand.None;
import picocli.CommandLine.Command;

@Command(
    name = "data-api",
    description = "Easily work with the Data API @|italic (beta)|@",
    subcommands = {
        DataAPIReplCmd.class,
        DataAPIExecCmd.class,
    }
)
@AliasForSubcommand(None.class)
public class DataAPICmd extends AbstractCmd {}
