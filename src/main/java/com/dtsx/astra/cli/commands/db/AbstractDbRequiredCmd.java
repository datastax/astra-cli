package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.completions.impls.DbNamesCompletion;
import com.dtsx.astra.cli.core.models.DbRef;
import picocli.CommandLine.Parameters;

public abstract class AbstractDbRequiredCmd<OpRes> extends AbstractDbCmd<OpRes> {
    @Parameters(
        completionCandidates = DbNamesCompletion.class,
        paramLabel = "DB",
        description = "The name or ID of the Astra database to operate on"
    )
    protected DbRef $dbRef;
}
