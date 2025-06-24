package com.dtsx.astra.cli.operations.db.cdc;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.gateways.db.cdc.CdcGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cdc.CdcCreateOperation.CdcCreateResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class CdcCreateOperation implements Operation<CdcCreateResult> {
    private final CdcGateway cdcGateway;
    private final CdcCreateRequest request;

    public sealed interface CdcCreateResult {}
    public record CdcAlreadyExists() implements CdcCreateResult {}
    public record CdcIllegallyAlreadyExists() implements CdcCreateResult {}
    public record CdcCreated() implements CdcCreateResult {}

    public record CdcCreateRequest(
        TableRef tableRef,
        TenantName tenantName,
        int topicPartitions,
        boolean ifNotExists
    ) {}

    @Override
    public CdcCreateResult execute() {
        val status = cdcGateway.create(request.tableRef, request.tenantName, request.topicPartitions);

        return switch (status) {
            case CreationStatus.Created<?> _ -> handleCdcCreated();
            case CreationStatus.AlreadyExists<?> _ -> handleCdcAlreadyExists(request.tableRef, request.tenantName, request.ifNotExists);
        };
    }

    private CdcCreateResult handleCdcCreated() {
        return new CdcCreated();
    }

    private CdcCreateResult handleCdcAlreadyExists(TableRef tableRef, TenantName tenantName, boolean ifNotExists) {
        if (ifNotExists) {
            return new CdcAlreadyExists();
        } else {
            return new CdcIllegallyAlreadyExists();
        }
    }

}
