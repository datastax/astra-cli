package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputHuman;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.DbStatusOperation;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import picocli.CommandLine.Command;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.DbStatusOperation.*;

@Command(
    name = "status",
    description = "Get the current status of a database."
)
@Example(
    comment = "Get the status of a database",
    command = "astra db status my_db"
)
public class DbStatusCmd extends AbstractDbSpecificCmd<DatabaseStatusType> {
    @Override
    protected DbStatusOperation mkOperation() {
        return new DbStatusOperation(dbGateway, new DbStatusRequest($dbRef));
    }

    @Override
    protected final OutputHuman executeHuman(DatabaseStatusType result) {
        return OutputHuman.message("Database %s is %s".formatted(highlight($dbRef), highlight(result)));
    }

    @Override
    protected final OutputAll execute(DatabaseStatusType result) {
        return OutputAll.serializeValue(result);
    }
}
