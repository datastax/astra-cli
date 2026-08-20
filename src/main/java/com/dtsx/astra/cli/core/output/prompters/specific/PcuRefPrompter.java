package com.dtsx.astra.cli.core.output.prompters.specific;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.models.PcuRef;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsClearAfterSelection;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsFallback;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.sdk.pcu.domain.PCUGroup;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static com.dtsx.astra.cli.core.output.ExitCode.PCU_GROUP_NOT_FOUND;
import static com.dtsx.astra.sdk.utils.IdUtils.isUUID;

public class PcuRefPrompter {
    public static PcuRef prompt(CliContext ctx, PcuGateway gateway, String prompt, UnaryOperator<NEList<PCUGroup>> modifier, Function<NeedsFallback<PCUGroup>, NeedsClearAfterSelection<PCUGroup>> fix) {
        return SpecificPrompter.<PCUGroup, PcuRef>run(ctx, (b) -> b
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
                    : pcu.getId().toString()
            ))
            .modifier(modifier)
            .fix(fix)
            .mapSingleFound(pcu -> PcuRef.fromTitleUnsafe(pcu.getTitle()))
            .mapMultipleFound(pcu -> PcuRef.fromId(pcu.getId()))
        );
    }

    private static String getIdentifier(PCUGroup group) {
        return Objects.requireNonNullElse(group.getTitle(), group.getId().toString());
    }

    private static String getRegion(PCUGroup group) {
        return group.getRegion() != null ? " " + group.getRegion() : "";
    }
}
