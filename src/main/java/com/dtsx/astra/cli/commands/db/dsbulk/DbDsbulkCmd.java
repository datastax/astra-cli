package com.dtsx.astra.cli.commands.db.dsbulk;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.mixins.HelpMixin;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(
    name = "dsbulk",
    description = "Use dsbulk to interface with your Astra DB database",
    subcommands = {
        DbDsbulkCountCmd.class,
        DbDsbulkLoadCmd.class,
        DbDsbulkUnloadCmd.class,
        DbDsbulkVersionCmd.class,
        DbDsbulkPathCmd.class,
    }
)
@Example(
    comment = "Count all rows in a database table",
    command = "astra db dsbulk count my_db -k my_keyspace -t my_table"
)
@Example(
    comment = "Load data into a database table from a file",
    command = "astra db dsbulk load my_db -k my_keyspace -t my_table --url my_data.csv"
)
@Example(
    comment = "Unload a database table",
    command = "astra db dsbulk unload my_db -k my_keyspace -t my_table"
)
@Example(
    comment = "Display dsbulk's version information",
    command = "astra db dsbulk version"
)
@Example(
    comment = "Get the path to the dsbulk executable",
    command = "astra db dsbulk path"
)
public class DbDsbulkCmd {
    @Mixin
    public HelpMixin help;
}
