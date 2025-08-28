package com.dtsx.astra.cli.operations.db.region;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import static com.dtsx.astra.cli.operations.db.region.RegionDeleteOperation.RegionDeleteResult;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.ACTIVE;

@RequiredArgsConstructor
public class RegionDeleteOperation implements Operation<RegionDeleteResult> {
    private final RegionGateway regionGateway;
    private final DbGateway dbGateway;
    private final RegionDeleteRequest request;

    public sealed interface RegionDeleteResult {}
    public record RegionNotFound() implements RegionDeleteResult {}
    public record RegionDeleted() implements RegionDeleteResult {}
    public record RegionDeletedAndDbActive(Duration waitTime) implements RegionDeleteResult {}
    public record RegionIllegallyNotFound() implements RegionDeleteResult {}

    public record RegionDeleteRequest(
        DbRef dbRef,
        RegionName region,
        boolean ifExists,
        LongRunningOptions lrOptions
    ) {}

    @Override
    public RegionDeleteResult execute() {
        val status = regionGateway.delete(request.dbRef, request.region);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> handleRegionDeleted(request.dbRef, request.lrOptions);
            case DeletionStatus.NotFound<?> _ -> handleRegionNotFound(request.ifExists);
        };
    }

    private RegionDeleteResult handleRegionDeleted(DbRef dbRef, LongRunningOptions lrOptions) {
        if (lrOptions.dontWait()) {
            return new RegionDeleted();
        }

        val awaitedDuration = dbGateway.waitUntilDbStatus(dbRef, ACTIVE, lrOptions.timeout());
        return new RegionDeletedAndDbActive(awaitedDuration);
    }

    private RegionDeleteResult handleRegionNotFound(boolean ifExists) {
        if (ifExists) {
            return new RegionNotFound();
        } else {
            return new RegionIllegallyNotFound();
        }
    }
}
