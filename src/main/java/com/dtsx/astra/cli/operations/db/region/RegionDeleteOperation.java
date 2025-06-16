package com.dtsx.astra.cli.operations.db.region;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;

import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.ACTIVE;

@RequiredArgsConstructor
public class RegionDeleteOperation {
    private final RegionGateway regionGateway;
    private final DbGateway dbGateway;

    public sealed interface RegionDeleteResult {}
    public record RegionNotFound() implements RegionDeleteResult {}
    public record RegionDeleted() implements RegionDeleteResult {}
    public record RegionDeletedAndDbActive(Duration waitTime) implements RegionDeleteResult {}

    public RegionDeleteResult execute(DbRef dbRef, String region, boolean ifExists, boolean dontWait, int timeout) {
        val status = regionGateway.deleteRegion(dbRef, region);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> handleRegionDeleted(dbRef, dontWait, timeout);
            case DeletionStatus.NotFound<?> _ -> handleRegionNotFound(dbRef, region, ifExists);
        };
    }

    private RegionDeleteResult handleRegionDeleted(DbRef dbRef, boolean dontWait, int timeout) {
        if (dontWait) {
            return new RegionDeleted();
        }

        val awaitedDuration = dbGateway.waitUntilDbStatus(dbRef, ACTIVE, timeout);
        return new RegionDeletedAndDbActive(awaitedDuration);
    }

    private RegionDeleteResult handleRegionNotFound(DbRef dbRef, String region, boolean ifExists) {
        if (ifExists) {
            return new RegionNotFound();
        } else {
            throw new RegionNotFoundException(region, dbRef);
        }
    }

    public static class RegionNotFoundException extends AstraCliException {
        public RegionNotFoundException(String region, DbRef dbRef) {
            super("""
              @|bold,red Error: Region '%s' does not exist in database '%s'.|@
            
              This may be expected, but to avoid this error:
              - Run %s to see the existing regions.
              - Pass the %s flag to skip this error if the region doesn't exist.
            """.formatted(
                region,
                dbRef,
                AstraColors.highlight("astra db list-regions " + dbRef),
                AstraColors.highlight("--if-exists")
            ));
        }
    }
}
