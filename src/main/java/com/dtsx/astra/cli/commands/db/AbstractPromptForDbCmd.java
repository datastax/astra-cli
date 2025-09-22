package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.CliConstants.$Db;
import com.dtsx.astra.cli.core.completions.impls.DbNamesCompletion;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.DbRef;
import lombok.val;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Parameters;

import java.util.UUID;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.core.output.ExitCode.DATABASE_NOT_FOUND;

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
            $dbRef = promptForDbRef(dbRefPrompt());
        }
    }

    protected abstract String dbRefPrompt();

    private DbRef promptForDbRef(String prompt) {
        val dbs = NEList.parse(dbGateway.findAll().toList()).orElseThrow(() -> new AstraCliException(DATABASE_NOT_FOUND, "@|bold,red No databases found to select from|@"));

        val namesAreUnique = dbs.stream()
            .map(db -> db.getInfo().getName())
            .distinct()
            .count() == dbs.size();

        val maxNameLength = dbs.stream()
            .map(db -> db.getInfo().getName().length())
            .max(Integer::compareTo)
            .orElse(0);

        val dbToDisplayMap = dbs.stream().collect(Collectors.toMap(
            db -> db,
            db -> db.getInfo().getName() + " ".repeat(maxNameLength - db.getInfo().getName().length()) +
                (namesAreUnique
                    ? " " + ctx.colors().NEUTRAL_500.use("(" + db.getInfo().getCloudProvider().name() + " " + db.getInfo().getRegion() + ")")
                    : " " + ctx.colors().NEUTRAL_500.use("(" + db.getId() + ")"))
        ));

        val db = ctx.console().select(prompt)
            .options(dbs)
            .requireAnswer()
            .mapper(dbToDisplayMap::get)
            .fallbackIndex(0)
            .fix(originalArgs(), "<db>")
            .clearAfterSelection();

        val multipleDbsMatch = dbs.stream().filter(d -> d.getInfo().getName().equals(db.getInfo().getName())).count() > 1;

        return (multipleDbsMatch)
            ? DbRef.fromId(UUID.fromString(db.getId()))
            : DbRef.fromNameUnsafe(db.getInfo().getName());
    }
}
