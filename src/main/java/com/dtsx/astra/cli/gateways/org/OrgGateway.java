package com.dtsx.astra.cli.gateways.org;

import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Optional;

public interface OrgGateway extends SomeGateway {
    Organization current();

    interface Stateless extends SomeGateway {
        Optional<Pair<AstraEnvironment, Organization>> resolveOrganizationEnvironment(AstraToken token);
    }
}
