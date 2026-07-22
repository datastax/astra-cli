package com.dtsx.astra.cli.gateways.db.clone;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.sdk.db.domain.DatabaseCloneStatus;
import com.dtsx.astra.sdk.db.domain.DatabaseSnapshot;

import java.time.Duration;
import java.util.Optional;
import java.util.stream.Stream;

public interface DbCloneGateway extends SomeGateway {
    DatabaseCloneStatus cloneFrom(DbRef targetDbRef, DbRef sourceDbRef, String snapshotId, Optional<String> sourceRegion);

    DatabaseCloneStatus getCloneStatus(DbRef targetDbRef, String operationId);

    Duration waitUntilCloneStatus(DbRef targetDbRef, String operationId, String targetStatus, Duration timeout);

    Stream<DatabaseSnapshot> findSnapshots(DbRef sourceDbRef, Optional<String> region, Optional<String> from, Optional<String> to);
}
