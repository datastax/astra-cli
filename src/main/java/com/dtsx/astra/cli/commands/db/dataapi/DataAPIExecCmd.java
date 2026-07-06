package com.dtsx.astra.cli.commands.db.dataapi;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "exec",
    description = "Execute Data API code against a database"
)
@Example(
    comment = "Insert a document using astra-db-ts (passing the -p flag to node)",
    command = "${cli.name} db data-api exec my_db -c my_coll -e \"my_coll.insertOne({ name: 'Joe' })\" -- -p"
)
@Example(
    comment = "Find a row using python",
    command = "${cli.name} db data-api exec my_db -l python -t my_table -e \"print(my_table.find_one())\""
)
@Example(
    comment = "Execute a file",
    command = "${cli.name} db data-api exec my_db -e @code.js"
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
