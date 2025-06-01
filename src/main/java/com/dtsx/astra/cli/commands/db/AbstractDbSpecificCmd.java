package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.completions.impls.DbNamesCompletion;
import picocli.CommandLine.*;

public abstract class AbstractDbSpecificCmd extends AbstractDbCmd {
    @Parameters(index = "0", completionCandidates = DbNamesCompletion.class)
    protected String dbName;
}
