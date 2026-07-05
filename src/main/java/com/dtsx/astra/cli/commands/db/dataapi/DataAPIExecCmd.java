package com.dtsx.astra.cli.commands.db.dataapi;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "exec",
    description = "Execute Data API code for a database"
)
public class DataAPIExecCmd extends DataAPIStartImpl {
    @Option(
        names = { "-e", "--execute" },
        description = "Code to execute using the Data API",
        paramLabel = "CODE"
    )
    public String $execCode = "";

    @Override
    protected String code() {
        return $execCode;
    }

    @Override
    protected boolean isRepl() {
        return false;
    }
}
