package com.dtsx.astra.cli.operations.org;

import com.dtsx.astra.cli.gateways.org.OrgGateway;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgIdOperation {
    private final OrgGateway orgGateway;

    public String execute() {
        return orgGateway.getCurrentOrg().getId();
    }
}
