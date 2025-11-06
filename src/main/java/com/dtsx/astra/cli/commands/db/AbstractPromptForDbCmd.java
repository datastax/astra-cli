package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.CliConstants.$Db;
import com.dtsx.astra.cli.core.completions.impls.DbNamesCompletion;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.prompters.specific.DbRefPrompter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Parameters;

public abstract class AbstractPromptForDbCmd<OpRes> extends AbstractDbCmd<OpRes> {
    @Parameters(
        arity = "0..1",
        completionCandidates = DbNamesCompletion.class,
        description = "The name or ID of the Astra database to operate on",
        paramLabel = $Db.LABEL
    )
    protected DbRef $dbRef;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();

        if ($dbRef == null) {
            $dbRef = DbRefPrompter.prompt(ctx, dbGateway, dbRefPrompt(), (b) -> b.fallbackIndex(0).fix(originalArgs(), "<db>"));
        }
    }

    protected abstract String dbRefPrompt();
}
