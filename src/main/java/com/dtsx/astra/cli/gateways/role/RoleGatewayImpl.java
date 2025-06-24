package com.dtsx.astra.cli.gateways.role;

import com.dtsx.astra.cli.core.exceptions.role.RoleNotFoundException;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.cli.utils.StringUtils;
import com.dtsx.astra.sdk.org.domain.Role;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;

@RequiredArgsConstructor
public class RoleGatewayImpl implements RoleGateway {
    private final APIProvider apiProvider;

    @Override
    public Optional<Role> tryFindOne(String role) {
        if (StringUtils.isUUID(role)) {
            return AstraLogger.loading("Looking up role by ID " + highlight(role), (_) -> 
                apiProvider.astraOpsClient().roles().find(role));
        }
        return AstraLogger.loading("Looking up role by name " + highlight(role), (_) -> 
            apiProvider.astraOpsClient().roles().findByName(role));
    }

    @Override
    public Role findOne(String role) {
        return tryFindOne(role).orElseThrow(() -> new RoleNotFoundException(role));
    }
}
