package com.dtsx.astra.cli.commands.pcu;

import com.dtsx.astra.cli.core.CliConstants.$Pcu;
import com.dtsx.astra.cli.core.completions.impls.PcuGroupsCompletion;
import com.dtsx.astra.cli.core.models.PcuRef;
import picocli.CommandLine.Parameters;

public abstract class AbstractPcuRequiredCmd<OpRes> extends AbstractPcuCmd<OpRes> {
    @Parameters(
        completionCandidates = PcuGroupsCompletion.class,
        description = "The name or ID of the PCU group to operate on",
        paramLabel = $Pcu.LABEL
    )
    protected PcuRef $pcuRef;
}
