package com.dtsx.astra.cli.operations.org;

import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.org.domain.Organization;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgGetOperation implements Operation<Organization> {
    private final OrgGateway orgGateway;
    private final Profile profile;

    @Override
    public Organization execute() {
        return orgGateway.find(profile.token(), profile.env());
    }
}
