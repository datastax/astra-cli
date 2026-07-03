package com.dtsx.astra.cli.core.output.prompters.specific;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsClearAfterSelection;
import com.dtsx.astra.cli.core.output.prompters.builders.SelectorBuilder.NeedsFallback;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway.RegionInfo;
import lombok.val;

import java.util.Comparator;
import java.util.function.Function;

import static com.dtsx.astra.cli.core.output.ExitCode.REGION_NOT_FOUND;

public class RegionNamePrompter {
    public record RegionCandidate(CloudProvider cloudProvider, String name, RegionInfo info) {}

    private static final Comparator<RegionCandidate> COMPARATOR = Comparator
        .<RegionCandidate, Boolean>comparing(c -> c.info.hasFreeTier()).reversed()
        .thenComparing(RegionCandidate::name);

    public static RegionCandidate prompt(CliContext ctx, RegionGateway gateway, boolean nonVector, String prompt, Function<NeedsFallback<RegionCandidate>, NeedsClearAfterSelection<RegionCandidate>> fix) {
        return SpecificPrompter.<RegionCandidate, RegionCandidate>run(ctx, (b) -> b
            .thing("region")
            .prompt(prompt)
            .thingNotFoundCode(REGION_NOT_FOUND)
            .thingsSupplier(() -> {
                return gateway.findAllServerless(!nonVector, false).entrySet().stream()
                    .flatMap(entry -> entry.getValue().entrySet().stream().map(regionEntry -> new RegionCandidate(entry.getKey(), regionEntry.getKey(), regionEntry.getValue())))
                    .sorted(COMPARATOR)
                    .toList();
            })
            .getThingIdentifier(RegionCandidate::name)
            .getThingDisplayExtra((c, unique) -> {
                val extra = (unique)
                    ? c.cloudProvider().name()
                    : c.cloudProvider().name() + " " + c.name();

                var formattedExtra = ctx.colors().NEUTRAL_500.use("(" + extra + ")");

                if (c.info().hasFreeTier()) {
                    formattedExtra += ctx.colors().PURPLE_300.use(" (free)");
                }

                return formattedExtra;
            })
            .fix(fix)
            .mapSingleFound(c -> c)
            .mapMultipleFound(c -> c)
        );
    }
}
