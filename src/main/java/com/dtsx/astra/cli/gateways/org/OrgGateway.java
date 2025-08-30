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

    Organization current();

    interface Stateless {

        Optional<Pair<AstraEnvironment, Organization>> resolveOrganizationEnvironment(AstraToken token);
    }
}
