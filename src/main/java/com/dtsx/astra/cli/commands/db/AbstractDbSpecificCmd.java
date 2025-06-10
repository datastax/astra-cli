package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.completions.impls.DbNamesCompletion;
import com.dtsx.astra.cli.core.models.DbRef;
import picocli.CommandLine.*;

public abstract class AbstractDbSpecificCmd extends AbstractDbCmd {
    @Parameters(index = "0", completionCandidates = DbNamesCompletion.class, paramLabel = "DB")
    protected DbRef dbRef;
}
