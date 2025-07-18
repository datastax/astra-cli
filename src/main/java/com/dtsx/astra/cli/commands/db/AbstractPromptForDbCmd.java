package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.completions.impls.DbNamesCompletion;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import lombok.val;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Parameters;

import java.util.UUID;
import java.util.stream.Collectors;

public abstract class AbstractPromptForDbCmd<OpRes> extends AbstractDbCmd<OpRes> {
    @Parameters(
        arity = "0..1",
        completionCandidates = DbNamesCompletion.class,
        paramLabel = "DB",
        description = "The name or ID of the Astra database to operate on"
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
        val dbs = dbGateway.findAll().toList();

        if (dbs.isEmpty()) {
            throw new AstraCliException("""
              @|bold,red No databases found to select from|@
            """);
        }

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
                    ? " " + AstraColors.NEUTRAL_500.use("(" + db.getInfo().getCloudProvider().name() + " " + db.getInfo().getRegion() + ")")
                    : " " + AstraColors.NEUTRAL_500.use("(" + db.getId() + ")"))
        ));

        val neDbs = NEList.of(dbs);

        val dbInput = AstraConsole.select(prompt)
            .options(neDbs)
            .mapper(dbToDisplayMap::get)
            .fallbackIndex(0)
            .fix(originalArgs(), "<db>")
            .clearAfterSelection();

        return dbInput
            .map(db ->
                (dbs.stream().filter(d -> d.getInfo().getName().equals(db.getInfo().getName())).count() > 1)
                    ? DbRef.fromId(UUID.fromString(db.getId()))
                    : DbRef.fromNameUnsafe(db.getInfo().getName())
            )
            .orElseThrow();
    }
}
