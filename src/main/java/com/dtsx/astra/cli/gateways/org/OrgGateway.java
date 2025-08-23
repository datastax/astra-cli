package com.dtsx.astra.cli.gateways.org;

import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.gateways.org.OrgGatewayImpl.StatelessImpl;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.graalvm.collections.Pair;

import java.util.Optional;

public interface OrgGateway {
    static OrgGateway mkDefault(AstraToken token, AstraEnvironment env) {
        return new OrgGatewayImpl(APIProvider.mkDefault(token, env));
    }

    Organization current();

    interface Stateless {
        static OrgGateway.Stateless mkDefault() {
            return new StatelessImpl();
        }

        Optional<Pair<AstraEnvironment, Organization>> resolveOrganizationEnvironment(AstraToken token);
    }
}
