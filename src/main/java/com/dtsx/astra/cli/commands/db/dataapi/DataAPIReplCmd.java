package com.dtsx.astra.cli.commands.db.dataapi;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "repl",
    description = "Start an interactive Data API REPL session against a database"
)
@Example(
    comment = "Start a REPL session using astra-db-ts",
    command = "${cli.name} db data-api repl my_db"
)
@Example(
    comment = "Start a REPL session using astrapy",
    command = "${cli.name} db data-api repl my_db -l python"
)
@Example(
    comment = "Start a REPL session with multiple collections available",
    command = "${cli.name} db data-api repl my_db -c coll1 -c coll2"
)
@Example(
    comment = "Start a REPL session, prompting for the collections/tables to include",
    command = "${cli.name} db data-api repl my_db -c -t"
)
@Example(
    comment = "Start a Data API REPL for a database and execute some initialization code",
    command = "${cli.name} db data-api repl my_db -e \"console.log('hello');\""
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
