package com.dtsx.astra.cli.commands.db.dataapi;

import com.dtsx.astra.cli.commands.user.AbstractCmd;
import picocli.CommandLine.Command;

@Command(
    name = "data-api",
    description = "Easily work with the Data API @|italic (beta)|@",
    subcommands = {
        DataAPIReplCmd.class,
        DataAPIExecCmd.class,
    }
)
public class DataAPICmd extends AbstractCmd {}
