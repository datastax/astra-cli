package com.dtsx.astra.cli.commands.db.cqlsh;

import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshVersionOperation;
import com.dtsx.astra.cli.operations.db.cqlsh.DbCqlshVersionOperation.CqlshVersionRequest;
import picocli.CommandLine.Command;

@Command(
    name = "version",
    description = "Display the currently installed cqlsh's version"
)
@Example(
    comment = "Display cqlsh's version information",
    command = "${cli.name} db cqlsh version"
)
public class DbCqlshVersionCmd extends AbstractCqlshExecCmd {
    @Override
    protected DbCqlshVersionOperation mkOperation() {
        return new DbCqlshVersionOperation(dbGateway, downloadsGateway, new CqlshVersionRequest(
            $debug,
            $encoding,
            $connectTimeout
        ));
    }
}
