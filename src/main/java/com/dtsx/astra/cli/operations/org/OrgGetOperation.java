package com.dtsx.astra.cli.operations.org;

import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.sdk.org.domain.Organization;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgGetOperation {
    private final OrgGateway orgGateway;

    public Organization execute() {
        return orgGateway.getCurrentOrg();
    }
}