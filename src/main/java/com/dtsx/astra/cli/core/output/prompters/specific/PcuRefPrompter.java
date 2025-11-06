package com.dtsx.astra.cli.core.output.prompters.specific;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsClearAfterSelection;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsFallback;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.cli.gateways.pcu.vendored.domain.PcuGroup;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import static com.dtsx.astra.cli.core.output.ExitCode.PCU_GROUP_NOT_FOUND;
import static com.dtsx.astra.sdk.utils.IdUtils.isUUID;

public class PcuRefPrompter {
    public static PcuRef prompt(CliContext ctx, PcuGateway gateway, String prompt, Function<NEList<PcuGroup>, NEList<PcuGroup>> modifier, Function<NeedsFallback<PcuGroup>, NeedsClearAfterSelection<PcuGroup>> fix) {
        return SpecificPrompter.<PcuGroup, PcuRef>run(ctx, (b) -> b
            .thing("PCU group")
            .prompt(prompt)
            .thingNotFoundCode(PCU_GROUP_NOT_FOUND)
            .thingsSupplier(() -> gateway.findAll().toList())
            .getThingIdentifier(PcuRefPrompter::getIdentifier)
            .getThingDisplayExtra((pcu, unique) -> (
                (unique)
                    ? pcu.getCloudProvider().name() + getRegion(pcu) :
                (isUUID(getIdentifier(pcu)))
                    ? ""
                    : pcu.getId()

            ))
            .modifier(modifier)
            .fix(fix)
            .mapSingleFound(pcu -> PcuRef.fromTitleUnsafe(pcu.getTitle()))
            .mapMultipleFound(pcu -> PcuRef.fromId(UUID.fromString(pcu.getId())))
        );
    }

    private static String getIdentifier(PcuGroup group) {
        return Objects.requireNonNullElse(group.getTitle(), group.getId());
    }

    private static String getRegion(PcuGroup group) {
        return group.getRegion() != null ? " " + group.getRegion() : "";
    }
}
