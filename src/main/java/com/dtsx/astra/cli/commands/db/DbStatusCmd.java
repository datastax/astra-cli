package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.db.DbStatusOperation;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import picocli.CommandLine.Command;

import java.util.function.Supplier;

import static com.dtsx.astra.cli.operations.db.DbStatusOperation.*;

@Command(
    name = "status",
    description = "Get the current status of a database."
)
@Example(
    comment = "Get the status of a database",
    command = "${cli.name} db status my_db"
)
public class DbStatusCmd extends AbstractPromptForDbCmd<DatabaseStatusType> {
    @Override
    protected DbStatusOperation mkOperation() {
        return new DbStatusOperation(ctx, dbGateway, new DbStatusRequest($dbRef));
    }

    @Override
    protected final OutputHuman executeHuman(Supplier<DatabaseStatusType> result) {
        return OutputHuman.response("Database %s is %s".formatted(ctx.highlight($dbRef), ctx.highlight(result.get())));
    }

    @Override
    protected final OutputAll execute(Supplier<DatabaseStatusType> result) {
        return OutputAll.serializeValue(result);
    }

    @Override
    protected String dbRefPrompt() {
        return "Select the database to get the status for";
    }
}
