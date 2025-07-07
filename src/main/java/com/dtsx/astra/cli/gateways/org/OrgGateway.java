package com.dtsx.astra.cli.gateways.org;

import com.dtsx.astra.cli.core.models.Token;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

public interface OrgGateway {
    static OrgGateway mkDefault(Token token, AstraEnvironment env) {
        return new OrgGatewayImpl(APIProvider.mkDefault(token, env));
    }

    Organization current();
}
