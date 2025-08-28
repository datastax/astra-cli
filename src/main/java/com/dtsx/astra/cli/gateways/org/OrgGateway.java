package com.dtsx.astra.cli.gateways.org;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.org.OrgGatewayImpl.StatelessImpl;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.graalvm.collections.Pair;

import java.util.Optional;

public interface OrgGateway {
    static OrgGateway mkDefault(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return new OrgGatewayImpl(ctx, APIProvider.mkDefault(token, env, ctx));
    }

    Organization current();

    interface Stateless {
        static OrgGateway.Stateless mkDefault(CliContext ctx) {
            return new StatelessImpl(ctx);
        }

        Optional<Pair<AstraEnvironment, Organization>> resolveOrganizationEnvironment(AstraToken token);
    }
}
