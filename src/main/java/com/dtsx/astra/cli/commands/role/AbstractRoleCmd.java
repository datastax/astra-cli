package com.dtsx.astra.cli.commands.role;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd;
import com.dtsx.astra.cli.core.completions.caches.RoleCompletionsCache;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractRoleCmd<OpRes> extends AbstractConnectedCmd<OpRes> {
    protected RoleGateway roleGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        roleGateway = RoleGateway.mkDefault(profile().token(), profile().env(), new RoleCompletionsCache());
    }
}
