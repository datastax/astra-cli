package com.dtsx.astra.cli.commands.db.region.regions.legacy;

import com.dtsx.astra.cli.commands.db.region.regions.AbstractRegionListCmd;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.db.region.AbstractRegionListOperation.FoundRegion;

import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractLegacyRegionListCmd extends AbstractRegionListCmd {
    @Override
    protected final OutputAll execute(Supplier<Stream<FoundRegion>> result) {
        ctx.log().warn("@!astra db %s!@ is deprecated".formatted(spec.commandLine().getCommandName()));
        ctx.log().warn("Use the new command @!astra db regions %s!@ instead".formatted(spec.commandLine().getCommandName().replace("list-regions-", "")));
        return super.execute(result);
    }
}
