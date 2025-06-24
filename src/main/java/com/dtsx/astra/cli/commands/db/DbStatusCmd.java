package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.output.output.OutputHuman;
import com.dtsx.astra.cli.operations.db.DbGetOperation;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputJson;
import com.dtsx.astra.cli.core.output.table.RenderableShellTable;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.db.DbStatusOperation;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.List;
import java.util.Optional;

import static com.dtsx.astra.cli.commands.db.DbGetCmd.DbGetKeys.*;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "status"
)
public final class DbStatusCmd extends AbstractDbSpecificCmd {
    @Override
    public OutputHuman executeHuman() {
        return OutputHuman.message("Database %s is %s".formatted(highlight(dbRef), highlight(fetchStatus())));
    }

    @Override
    public OutputAll execute() {
        return OutputAll.serializeValue(fetchStatus());
    }

    private DatabaseStatusType fetchStatus() {
        return new DbStatusOperation(dbGateway).execute(dbRef);
    }
}
