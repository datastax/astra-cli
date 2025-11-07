package com.dtsx.astra.cli.core.output.prompters.specific;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsClearAfterSelection;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsFallback;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.sdk.streaming.domain.Tenant;

import java.util.function.Function;

import static com.dtsx.astra.cli.core.output.ExitCode.TENANT_NOT_FOUND;

public class TenantPrompter {
    public static TenantName prompt(CliContext ctx, StreamingGateway gateway, String prompt, Function<NeedsFallback<Tenant>, NeedsClearAfterSelection<Tenant>> fix) {
        return SpecificPrompter.<Tenant, TenantName>run(ctx, (b) -> b
            .thing("tenant")
            .prompt(prompt)
            .thingNotFoundCode(TENANT_NOT_FOUND)
            .thingsSupplier(() -> gateway.findAll().toList())
            .getThingIdentifier(Tenant::getTenantName)
            .getThingDisplayExtra((tenant, _) -> (
                tenant.getCloudProvider() + " " + tenant.getCloudRegion()
            ))
            .fix(fix)
            .mapSingleFound(db -> TenantName.mkUnsafe(db.getTenantName()))
        );
    }
}
