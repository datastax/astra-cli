package com.dtsx.astra.cli.core.output.prompters.specific;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsClearAfterSelection;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsFallback;
import com.dtsx.astra.cli.gateways.db.clone.DbCloneGateway;
import com.dtsx.astra.sdk.db.domain.DatabaseSnapshot;

import java.util.Optional;
import java.util.function.Function;

import static com.dtsx.astra.cli.core.output.ExitCode.SNAPSHOT_NOT_FOUND;

public class SnapshotPrompter {
    public static String prompt(CliContext ctx, DbCloneGateway gateway, DbRef sourceDbRef, String prompt, Function<NeedsFallback<DatabaseSnapshot>, NeedsClearAfterSelection<DatabaseSnapshot>> fix) {
        return SpecificPrompter.<DatabaseSnapshot, String>run(ctx, (b) -> b
            .thing("snapshot")
            .prompt(prompt)
            .thingNotFoundCode(SNAPSHOT_NOT_FOUND)
            .thingsSupplier(() -> gateway.findSnapshots(sourceDbRef, Optional.empty(), Optional.empty(), Optional.empty()).toList())
            .getThingIdentifier(DatabaseSnapshot::getId)
            .getThingDisplayExtra((snapshot, _) -> snapshot.getTime())
            .fix(fix)
            .mapSingleFound(DatabaseSnapshot::getId)
            .mapMultipleFound(DatabaseSnapshot::getId)
        );
    }
}
