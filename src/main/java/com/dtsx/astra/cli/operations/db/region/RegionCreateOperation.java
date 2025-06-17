package com.dtsx.astra.cli.operations.db.region;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.ACTIVE;

@RequiredArgsConstructor
public class RegionCreateOperation {
    private final RegionGateway regionGateway;
    private final DbGateway dbGateway;

    public sealed interface RegionCreateResult {}
    public record RegionAlreadyExists() implements RegionCreateResult {}
    public record RegionCreated() implements RegionCreateResult {}
    public record RegionCreatedAndDbActive(Duration waitTime) implements RegionCreateResult {}

    public RegionCreateResult execute(DbRef dbRef, String region, boolean ifNotExists, LongRunningOptions lrOptions) {
        val dbInfo = dbGateway.findOneDb(dbRef);

        val status = regionGateway.createRegion(dbRef, region, dbInfo.getInfo().getTier(), dbInfo.getInfo().getCloudProvider());

        return switch (status) {
            case CreationStatus.Created<?> _ -> handleRegionCreated(dbRef, lrOptions);
            case CreationStatus.AlreadyExists<?> _ -> handleRegionAlreadyExists(dbRef, region, ifNotExists);
        };
    }

    private RegionCreateResult handleRegionCreated(DbRef dbRef, LongRunningOptions lrOptions) {
        if (lrOptions.dontWait()) {
            return new RegionCreated();
        }

        val awaitedDuration = dbGateway.waitUntilDbStatus(dbRef, ACTIVE, lrOptions.timeout());
        return new RegionCreatedAndDbActive(awaitedDuration);
    }

    private RegionCreateResult handleRegionAlreadyExists(DbRef dbRef, String region, boolean ifNotExists) {
        if (ifNotExists) {
            return new RegionAlreadyExists();
        } else {
            throw new RegionAlreadyExistsException(region, dbRef);
        }
    }

    public static class RegionAlreadyExistsException extends AstraCliException {
        public RegionAlreadyExistsException(String region, DbRef dbRef) {
            super("""
              @|bold,red Error: Region '%s' already exists in database '%s'.|@
            
              This may be expected, but to avoid this error:
              - Run %s to see the existing regions.
              - Pass the %s flag to skip this error if the region already exists.
            """.formatted(
                region,
                dbRef,
                AstraColors.highlight("astra db list-regions " + dbRef),
                AstraColors.highlight("--if-not-exists")
            ));
        }
    }
}
