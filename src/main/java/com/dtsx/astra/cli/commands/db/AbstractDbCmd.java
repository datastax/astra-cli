package com.dtsx.astra.cli.commands.db;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd;
import com.dtsx.astra.cli.completions.caches.DbCompletionsCache;
import com.dtsx.astra.cli.domain.db.DbService;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractDbCmd extends AbstractConnectedCmd {
    protected DbService dbService;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        dbService = DbService.mkDefault(profile().token(), profile().env(), new DbCompletionsCache(profile().name()));
    }
}
