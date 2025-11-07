package com.dtsx.astra.cli.commands.streaming;

import com.dtsx.astra.cli.core.completions.impls.TenantNamesCompletion;
import com.dtsx.astra.cli.core.models.TenantName;
import picocli.CommandLine.Parameters;

public abstract class AbstractStreamingTenantRequiredCmd<OpRes> extends AbstractStreamingCmd<OpRes> {
    @Parameters(
        paramLabel = "TENANT",
        description = "The name of the tenant to operate on",
        completionCandidates = TenantNamesCompletion.class
    )
    protected TenantName $tenantName;
}
