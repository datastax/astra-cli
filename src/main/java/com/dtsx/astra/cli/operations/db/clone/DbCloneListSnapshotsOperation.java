package com.dtsx.astra.cli.operations.db.clone;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.clone.DbCloneGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.sdk.db.domain.DatabaseSnapshot;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.stream.Stream;

@RequiredArgsConstructor
public class DbCloneListSnapshotsOperation implements Operation<Stream<DatabaseSnapshot>> {
    private final DbCloneGateway dbCloneGateway;
    private final DbCloneListSnapshotsRequest request;

    public record DbCloneListSnapshotsRequest(
        DbRef sourceDbRef,
        Optional<String> region,
        Optional<String> from,
        Optional<String> to
    ) {}

    @Override
    public Stream<DatabaseSnapshot> execute() {
        return dbCloneGateway.findSnapshots(request.sourceDbRef, request.region, request.from, request.to);
    }
}
