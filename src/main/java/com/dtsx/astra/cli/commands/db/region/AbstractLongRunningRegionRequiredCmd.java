package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.WithSetTimeout;
import picocli.CommandLine.Mixin;

public abstract class AbstractLongRunningRegionRequiredCmd extends AbstractRegionRequiredCmd implements WithSetTimeout {
    @Mixin
    protected LongRunningOptionsMixin lrMixin;
}
