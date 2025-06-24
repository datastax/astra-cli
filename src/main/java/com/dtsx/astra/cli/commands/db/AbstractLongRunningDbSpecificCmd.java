package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin;
import com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.WithSetTimeout;
import picocli.CommandLine.Mixin;

public abstract class AbstractLongRunningDbSpecificCmd<OpRes> extends AbstractDbSpecificCmd<OpRes> implements WithSetTimeout {
    @Mixin
    protected LongRunningOptionsMixin lrMixin;
}
