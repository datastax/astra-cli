package com.dtsx.astra.cli.operations.db.region;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.models.CloudProvider;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import static com.dtsx.astra.cli.operations.db.region.RegionCreateOperation.RegionCreateResult;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.ACTIVE;

@RequiredArgsConstructor
public class RegionCreateOperation implements Operation<RegionCreateResult> {
    private final RegionGateway regionGateway;
    private final DbGateway dbGateway;
    private final RegionCreateRequest request;

    public sealed interface RegionCreateResult {}
    public record RegionAlreadyExists() implements RegionCreateResult {}
    public record RegionCreated() implements RegionCreateResult {}
    public record RegionCreatedAndDbActive(Duration waitTime) implements RegionCreateResult {}
    public record RegionIllegallyAlreadyExists() implements RegionCreateResult {}

    public record RegionCreateRequest(
        DbRef dbRef,
        RegionName region,
        boolean ifNotExists,
        LongRunningOptions lrOptions
    ) {}

    @Override
    public RegionCreateResult execute() {
        val dbInfo = dbGateway.findOne(request.dbRef);

        val status = regionGateway.create(
            request.dbRef,
            request.region,
            dbInfo.getInfo().getTier(),
            CloudProvider.fromSdkType(dbInfo.getInfo().getCloudProvider())
        );

        return switch (status) {
            case CreationStatus.Created<?> _ -> handleRegionCreated(request.dbRef, request.lrOptions);
            case CreationStatus.AlreadyExists<?> _ -> handleRegionAlreadyExists(request.ifNotExists);
        };
    }

    private RegionCreateResult handleRegionCreated(DbRef dbRef, LongRunningOptions lrOptions) {
        if (lrOptions.dontWait()) {
            return new RegionCreated();
        }

        val awaitedDuration = dbGateway.waitUntilDbStatus(dbRef, ACTIVE, lrOptions.timeout());
        return new RegionCreatedAndDbActive(awaitedDuration);
    }

    private RegionCreateResult handleRegionAlreadyExists(boolean ifNotExists) {
        if (ifNotExists) {
            return new RegionAlreadyExists();
        } else {
            return new RegionIllegallyAlreadyExists();
        }
    }
}
