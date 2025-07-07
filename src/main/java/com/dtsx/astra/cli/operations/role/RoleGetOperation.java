package com.dtsx.astra.cli.operations.role;

import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.org.domain.Role;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RoleGetOperation implements Operation<Role> {
    private final RoleGateway roleGateway;
    private final RoleGetRequest request;

    public record RoleGetRequest(RoleRef role) {}

    @Override
    public Role execute() {
        return roleGateway.findOne(request.role());
    }
}
