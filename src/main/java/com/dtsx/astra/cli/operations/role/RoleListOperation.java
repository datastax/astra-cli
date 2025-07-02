package com.dtsx.astra.cli.operations.role;

import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.role.RoleListOperation.RoleInfo;
import com.dtsx.astra.sdk.org.domain.Role;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class RoleListOperation implements Operation<Stream<RoleInfo>> {
    private final RoleGateway roleGateway;

    public record RoleInfo(
        String id,
        String name,
        String description,
        Role raw
    ) {}

    @Override
    public Stream<RoleInfo> execute() {
        return roleGateway.findAll()
            .map((role) -> new RoleInfo(
                role.getId(),
                role.getName(),
                role.getPolicy().getDescription(),
                role
            ));
    }
}
