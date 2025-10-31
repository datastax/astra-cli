package com.dtsx.astra.cli.commands.pcu;

import com.dtsx.astra.cli.core.CliConstants.$Pcu;
import com.dtsx.astra.cli.core.completions.impls.PcuGroupsCompletion;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroup;
import lombok.val;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Parameters;

import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.core.output.ExitCode.PCU_GROUP_NOT_FOUND;
import static com.dtsx.astra.sdk.utils.IdUtils.isUUID;

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
            $pcuRef = promptForPcuRef(pcuRefPrompt());
        }
    }

    protected abstract String pcuRefPrompt();

    protected boolean shouldPromptForPcuRef() {
        return $pcuRef == null;
    }

    protected NEList<PcuGroup> modifyPcusPromptList(NEList<PcuGroup> pcus) {
        return pcus;
    }

    private PcuRef promptForPcuRef(String prompt) {
        val pcus = modifyPcusPromptList(
            NEList.parse(pcuGateway.findAll().toList()).orElseThrow(() -> new AstraCliException(PCU_GROUP_NOT_FOUND, "@|bold,red No PCU groups found to select from|@"))
        );

        val namesAreUnique = pcus.stream()
            .map(this::getIdentifier)
            .distinct()
            .count() == pcus.size();

        val maxNameLength = pcus.stream()
            .map(pcu -> getIdentifier(pcu).length())
            .max(Integer::compareTo)
            .orElse(0);

        val pcuToDisplayMap = pcus.stream().collect(Collectors.toMap(
            pcu -> pcu,
            pcu -> getIdentifier(pcu) + " ".repeat(maxNameLength - getIdentifier(pcu).length()) +
                (namesAreUnique
                    ? " " + ctx.colors().NEUTRAL_500.use("(" + pcu.getCloudProvider().name() + getRegion(pcu) + ")") :
                (isUUID(getIdentifier(pcu)))
                    ? ""
                    : " " + ctx.colors().NEUTRAL_500.use("(" + pcu.getId() + ")"))
        ));

        val selected = ctx.console().select(prompt)
            .options(pcus)
            .requireAnswer()
            .mapper(pcuToDisplayMap::get)
            .fallbackIndex(0)
            .fix(originalArgs(), "<db>")
            .clearAfterSelection();

        val multiplePcusMatch = pcus.stream().filter(pcu -> getIdentifier(pcu).equals(getIdentifier(selected))).count() > 1;

        return (multiplePcusMatch || selected.getTitle() == null)
            ? PcuRef.fromId(UUID.fromString(selected.getId()))
            : PcuRef.fromTitleUnsafe(selected.getTitle());
    }

    private String getIdentifier(PcuGroup group) {
        return Objects.requireNonNullElse(group.getTitle(), group.getId());
    }

    private String getRegion(PcuGroup group) {
        return group.getRegion() != null ? " " + group.getRegion() : "";
    }
}
