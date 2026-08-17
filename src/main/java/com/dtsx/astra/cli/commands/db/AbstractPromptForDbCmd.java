package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.CliConstants.$Db;
import com.dtsx.astra.cli.core.completions.impls.DbNamesCompletion;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.prompters.specific.DbRefPrompter;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Parameters;

import java.util.function.Predicate;

import static com.dtsx.astra.cli.core.output.ExitCode.PCU_GROUP_NOT_FOUND;

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
            $dbRef = DbRefPrompter.prompt(ctx, dbGateway, dbRefPrompt(), this::dbRefPromptModifier, (b) -> b.fallbackIndex(0).fix(originalArgs(), "<db>"));
        }
    }

    protected abstract String dbRefPrompt();

    protected Pair<String, Predicate<DatabaseStatusType>> dbRefPromptFilter() {
        return Pair.of("", _ -> true);
    }

    private NEList<Database> dbRefPromptModifier(NEList<Database> dbs) {
        val p = dbRefPromptFilter();

        return NEList.parse(
            dbs.stream().filter(db -> p.getRight().test(db.getStatus())).toList()
        ).orElseThrow(
            () -> new AstraCliException(PCU_GROUP_NOT_FOUND, "@|bold,red No %s databases found to select from|@".formatted(p.getLeft()))
        );
    }
}
