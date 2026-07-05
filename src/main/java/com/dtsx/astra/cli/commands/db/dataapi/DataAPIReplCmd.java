package com.dtsx.astra.cli.commands.db.dataapi;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "repl",
    description = "Start an interactive Data API REPL for a database"
)
public class DataAPIReplCmd extends DataAPIStartImpl {
    @Option(
        names = { "-e", "--extra" },
        description = "Code to execute after starting the REPL (e.g., to import modules or set up the environment)",
        paramLabel = "CODE"
    )
    public String $extraCode = "";

    @Override
    protected String code() {
        return $extraCode;
    }

    @Override
    protected boolean isRepl() {
        return true;
    }
}
