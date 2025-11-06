package com.dtsx.astra.cli.core.output.prompters.specific;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsClearAfterSelection;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsFallback;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;

import java.util.function.Function;

import static com.dtsx.astra.cli.core.output.ExitCode.KEYSPACE_NOT_FOUND;

public class KeyspacePrompter {
    public static KeyspaceRef prompt(CliContext ctx, KeyspaceGateway gateway, DbRef db, String prompt, Function<NeedsFallback<String>, NeedsClearAfterSelection<String>> fix) {
        return SpecificPrompter.<String, KeyspaceRef>run(ctx, (b) -> b
            .thing("keyspace")
            .prompt(prompt)
            .thingNotFoundCode(KEYSPACE_NOT_FOUND)
            .thingsSupplier(() -> gateway.findAll(db).keyspaces())
            .getThingIdentifier(ks -> ks)
            .fix(fix)
            .mapSingleFound(ks -> KeyspaceRef.mkUnsafe(db, ks))
        );
    }
}
