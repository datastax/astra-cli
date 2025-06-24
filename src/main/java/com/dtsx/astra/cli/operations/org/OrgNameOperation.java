package com.dtsx.astra.cli.operations.org;

import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OrgNameOperation implements Operation<String> {
    private final OrgGateway orgGateway;

    @Override
    public String execute() {
        return orgGateway.getCurrentOrg().getName();
    }
}
