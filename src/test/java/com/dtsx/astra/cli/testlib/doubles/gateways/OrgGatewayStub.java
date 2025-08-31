package com.dtsx.astra.cli.testlib.doubles.gateways;

import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import org.graalvm.collections.Pair;

import java.util.Optional;

import static com.dtsx.astra.cli.testlib.doubles.Utils.methodIllegallyCalled;

public class OrgGatewayStub implements OrgGateway {
    @Override
    public Organization current() {
        return methodIllegallyCalled();
    }

    public static class StatelessImpl implements OrgGateway.Stateless {
        @Override
        public Optional<Pair<AstraEnvironment, Organization>> resolveOrganizationEnvironment(AstraToken token) {
            return methodIllegallyCalled();
        }
    }
}
