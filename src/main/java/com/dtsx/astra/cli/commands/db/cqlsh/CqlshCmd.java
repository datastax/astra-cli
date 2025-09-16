package com.dtsx.astra.cli.commands.db.cqlsh;

import com.dtsx.astra.cli.core.help.Example;
import picocli.CommandLine.Command;

@Command(
    name = "cqlsh",
    description = "Connect to your Astra database using cqlsh",
    subcommands = {
        CqlshStartCmd.class,
        CqlshExecCmd.class,
        CqlshVersionCmd.class,
        CqlshPathCmd.class,
    }
)
@Example(
    comment = "Launch cqlsh for a database",
    command = "${cli.name} db cqlsh start my_db"
)
@Example(
    comment = "Launch cqlsh with a specific keyspace",
    command = "${cli.name} db cqlsh start my_db -k my_keyspace"
)
@Example(
    comment = "Execute a CQL statement",
    command = "${cli.name} db cqlsh exec my_db \"SELECT * FROM my_keyspace.my_table\""
)
@Example(
    comment = "Execute a CQL file",
    command = "${cli.name} db cqlsh exec my_db -f script.cql"
)
public class CqlshCmd {}
