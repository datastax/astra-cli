package com.dtsx.astra.cli.commands.pcu;

import com.dtsx.astra.cli.core.CliConstants.$Pcu;
import com.dtsx.astra.cli.core.completions.impls.PcuGroupsCompletion;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.core.output.prompters.specific.PcuRefPrompter;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroup;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Parameters;

public abstract class AbstractPromptForPcuCmd<OpRes> extends AbstractPcuCmd<OpRes> {
    @Parameters(
        arity = "0..1",
        completionCandidates = PcuGroupsCompletion.class,
        description = "The name or ID of the PCU group to work with",
        paramLabel = $Pcu.LABEL
    )
    protected PcuRef $pcuRef;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();

        if (shouldPromptForPcuRef()) {
            $pcuRef = PcuRefPrompter.prompt(ctx, pcuGateway, pcuRefPrompt(), this::modifyPcusPromptList, (b) -> b.fallbackIndex(0).fix(originalArgs(), "<pcu>"));
        }
    }

    protected abstract String pcuRefPrompt();

    protected boolean shouldPromptForPcuRef() {
        return $pcuRef == null;
    }

    protected NEList<PcuGroup> modifyPcusPromptList(NEList<PcuGroup> pcus) {
        return pcus;
    }
}
