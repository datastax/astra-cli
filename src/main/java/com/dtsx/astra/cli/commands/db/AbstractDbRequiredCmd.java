package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.CliConstants.$Db;
import com.dtsx.astra.cli.core.completions.impls.DbNamesCompletion;
import com.dtsx.astra.cli.core.models.DbRef;
import picocli.CommandLine.Parameters;

public abstract class AbstractDbRequiredCmd<OpRes> extends AbstractDbCmd<OpRes> {
    @Parameters(
        completionCandidates = DbNamesCompletion.class,
        description = "The name or ID of the Astra database to operate on",
        paramLabel = $Db.LABEL
    )
    protected DbRef $dbRef;
}
