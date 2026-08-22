package com.dtsx.astra.cli.operations.org;

import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgIdOperation implements Operation<String> {
    private final OrgGateway orgGateway;
    private final Profile profile;

    @Override
    public String execute() {
        return orgGateway.find(profile.token(), profile.env()).getId();
    }
}
