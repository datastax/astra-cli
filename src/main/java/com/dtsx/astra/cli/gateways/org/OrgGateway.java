package com.dtsx.astra.cli.gateways.org;

import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

public interface OrgGateway {
    static OrgGateway mkDefault(AstraToken token, AstraEnvironment env) {
        return new OrgGatewayImpl(APIProvider.mkDefault(token, env));
    }

    Organization current();
}
