package com.dtsx.astra.cli.commands.token;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

public abstract class AbstractTokenCmd<OpRes> extends AbstractConnectedCmd<OpRes> {
    protected TokenGateway tokenGateway;
    protected RoleGateway roleGateway;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();
        tokenGateway = TokenGateway.mkDefault(profile().token(), profile().env());
        roleGateway = RoleGateway.mkDefault(profile().token(), profile().env());
    }
}
