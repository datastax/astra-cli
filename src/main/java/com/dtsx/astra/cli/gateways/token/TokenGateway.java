package com.dtsx.astra.cli.gateways.token;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.datatypes.NEList;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import com.dtsx.astra.sdk.org.domain.IamToken;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface TokenGateway extends SomeGateway {
    Stream<IamToken> findAll();

    boolean exists(String clientId);

    CreateTokenResponse create(NEList<RoleRef> refs, Optional<String> description, Optional<UUID> orgId, Optional<Instant> expiration);

    DeletionStatus<Void> delete(String clientId);

    void validate(AstraToken token);
}
