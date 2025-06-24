package com.dtsx.astra.cli.operations.db.cdc;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.CdcRef;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.cdc.CdcGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.time.Duration;

import static com.dtsx.astra.cli.core.mixins.LongRunningOptionsMixin.LongRunningOptions;
import static com.dtsx.astra.sdk.db.domain.DatabaseStatusType.ACTIVE;

@RequiredArgsConstructor
public class CdcCreateOperation {
    private final CdcGateway cdcGateway;

    public sealed interface CdcCreateResult {}
    public record CdcAlreadyExists() implements CdcCreateResult {}
    public record CdcCreated() implements CdcCreateResult {}

    public CdcCreateResult execute(TableRef tableRef, TenantName tenantName, int topicPartition, boolean ifNotExists) {
        val status = cdcGateway.create(tableRef, tenantName, topicPartition);

        return switch (status) {
            case CreationStatus.Created<?> _ -> handleCdcCreated();
            case CreationStatus.AlreadyExists<?> _ -> handleCdcAlreadyExists(tableRef, tenantName, ifNotExists);
        };
    }

    private CdcCreateResult handleCdcCreated() {
        return new CdcCreated();
    }

    private CdcCreateResult handleCdcAlreadyExists(TableRef tableRef, TenantName tenantName, boolean ifNotExists) {
        if (ifNotExists) {
            return new CdcAlreadyExists();
        } else {
            throw new CdcAlreadyExistsException(CdcRef.fromDefinition(tableRef, tenantName));
        }
    }

    public static class CdcAlreadyExistsException extends AstraCliException {
        public CdcAlreadyExistsException(CdcRef cdcRef) {
            super("""
              @|bold,red Error: Cdc '%s' already exists in database '%s'.|@
            
              This may be expected, but to avoid this error:
              - Run %s to see the existing cdcs.
              - Pass the %s flag to skip this error if the cdc already exists.
            """.formatted(
                cdcRef,
                cdcRef.db(),
                AstraColors.highlight("astra db list-cdcs " + cdcRef.db()),
                AstraColors.highlight("--if-not-exists")
            ));
        }
    }
}
