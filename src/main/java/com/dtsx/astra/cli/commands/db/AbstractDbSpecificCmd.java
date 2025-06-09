package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.completions.impls.DbNamesCompletion;
import com.dtsx.astra.cli.domain.db.DbRef;
import picocli.CommandLine.*;

public abstract class AbstractDbSpecificCmd extends AbstractDbCmd {
    @Parameters(index = "0", completionCandidates = DbNamesCompletion.class, paramLabel = "DB")
    protected DbRef dbRef;
}
