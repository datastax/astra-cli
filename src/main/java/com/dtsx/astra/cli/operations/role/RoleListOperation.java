package com.dtsx.astra.cli.operations.role;

import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.role.RoleListOperation.RoleInfo;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class RoleListOperation implements Operation<List<RoleInfo>> {
    private final RoleGateway roleGateway;

    public record RoleInfo(
        String id,
        String name,
        String description
    ) {}

    @Override
    public List<RoleInfo> execute() {
        return roleGateway.findAll().stream()
            .map(role -> new RoleInfo(
                role.getId(),
                role.getName(),
                role.getPolicy().getDescription()
            ))
            .toList();
    }
}
