package com.dtsx.astra.cli.commands.db.region;

import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.WithSetTimeout;
import picocli.CommandLine.Mixin;

public abstract class AbstractLongRunningRegionRequiredCmd<OpRes> extends AbstractRegionRequiredCmd<OpRes> implements WithSetTimeout {
    @Mixin
    protected LongRunningOptionsMixin lrMixin;
}
