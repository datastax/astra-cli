package com.dtsx.astra.cli.gateways.user;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.CompletionsCache;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.models.UserRef;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.role.RoleGatewayImpl;
import com.dtsx.astra.sdk.org.domain.User;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public interface UserGateway {
    static UserGateway mkDefault(AstraToken token, AstraEnvironment env, CompletionsCache userCompletionsCache, CliContext ctx) {
        return new UserGatewayCompletionsCacheWrapper(new UserGatewayImpl(ctx, APIProvider.mkDefault(token, env, ctx), new RoleGatewayImpl(ctx, APIProvider.mkDefault(token, env, ctx))), userCompletionsCache);
    }

    User findOne(UserRef user);

    Stream<User> findAll();

    CreationStatus<List<UUID>> invite(UserRef user, List<RoleRef> roles);

    DeletionStatus<Void> delete(UserRef user);
}
