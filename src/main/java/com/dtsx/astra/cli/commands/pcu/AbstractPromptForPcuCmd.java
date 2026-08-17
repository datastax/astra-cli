package com.dtsx.astra.cli.commands.pcu;

import com.dtsx.astra.cli.core.CliConstants.$Pcu;
import com.dtsx.astra.cli.core.completions.impls.PcuGroupsCompletion;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.core.models.PcuStatus;
import com.dtsx.astra.cli.core.output.prompters.specific.PcuRefPrompter;
import com.dtsx.astra.sdk.pcu.domain.PCUGroup;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Parameters;

import java.util.function.Predicate;

import static com.dtsx.astra.cli.core.output.ExitCode.PCU_GROUP_NOT_FOUND;

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
            $pcuRef = PcuRefPrompter.prompt(ctx, pcuGateway, pcuRefPrompt(), this::pcuRefPromptModifier, (b) -> b.fallbackIndex(0).fix(originalArgs(), "<pcu>"));
        }
    }

    protected abstract String pcuRefPrompt();

    protected boolean shouldPromptForPcuRef() {
        return $pcuRef == null;
    }

    protected Pair<String, Predicate<PcuStatus>> pcuRefPromptFilter() {
        return Pair.of("", _ -> true);
    }

    private NEList<PCUGroup> pcuRefPromptModifier(NEList<PCUGroup> pcus) {
        val p = pcuRefPromptFilter();

        return NEList.parse(
            pcus.stream().filter((pcu) -> p.getRight().test(new PcuStatus(pcu))).toList()
        ).orElseThrow(
            () -> new AstraCliException(PCU_GROUP_NOT_FOUND, "@|bold,red No %s PCU groups found to select from|@".formatted(p.getLeft()))
        );
    }
}
