package com.dtsx.astra.cli.core.output.prompters.specific;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsClearAfterSelection;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsFallback;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.sdk.db.domain.Database;

import java.util.function.Function;

import static com.dtsx.astra.cli.core.output.ExitCode.DATABASE_NOT_FOUND;

public class DbRefPrompter {
    public static DbRef prompt(CliContext ctx, DbGateway gateway, String prompt, Function<NeedsFallback<Database>, NeedsClearAfterSelection<Database>> fix) {
        return SpecificPrompter.<Database, DbRef>run(ctx, (b) -> b
            .thing("database")
            .prompt(prompt)
            .thingNotFoundCode(DATABASE_NOT_FOUND)
            .thingsSupplier(() -> gateway.findAll().toList())
            .getThingIdentifier(db -> db.getInfo().getName())
            .getThingDisplayExtra((db, unique) -> (
                (unique)
                    ? db.getInfo().getCloudProvider().name() + " " + db.getInfo().getRegion()
                    : db.getId()

            ))
            .fix(fix)
            .mapSingleFound(db -> DbRef.fromNameUnsafe(db.getInfo().getName()))
            .mapMultipleFound(db -> DbRef.fromId(java.util.UUID.fromString(db.getId())))
        );
    }
}
