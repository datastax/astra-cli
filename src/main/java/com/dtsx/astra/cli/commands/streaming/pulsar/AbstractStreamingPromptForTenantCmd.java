package com.dtsx.astra.cli.commands.streaming.pulsar;

import com.dtsx.astra.cli.commands.streaming.AbstractStreamingCmd;
import com.dtsx.astra.cli.core.completions.impls.TenantNamesCompletion;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.core.output.prompters.specific.TenantPrompter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Parameters;

public abstract class AbstractStreamingPromptForTenantCmd<OpRes> extends AbstractStreamingCmd<OpRes> {
    @Parameters(
        arity = "0..1",
        completionCandidates = TenantNamesCompletion.class,
        description = "The name of the tenant to operate on",
        paramLabel = "TENANT"
    )
    protected TenantName $tenantName;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();

        if ($tenantName == null) {
            $tenantName = TenantPrompter.prompt(ctx, streamingGateway, tenantNamePrompt(), (b) -> b.fallbackIndex(0).fix(originalArgs(), "<tenant>"));
        }
    }

    protected abstract String tenantNamePrompt();
}
