package com.dtsx.astra.cli.operations.db.cdc;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.CdcRef;
import com.dtsx.astra.cli.gateways.db.cdc.CdcGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cdc.CdcDeleteOperation.CdcDeleteResult;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class CdcDeleteOperation implements Operation<CdcDeleteResult> {
    private final CdcGateway cdcGateway;
    private final CdcDeleteRequest request;

    public sealed interface CdcDeleteResult {}
    public record CdcNotFound() implements CdcDeleteResult {}
    public record CdcIllegallyNotFound() implements CdcDeleteResult {}
    public record CdcDeleted() implements CdcDeleteResult {}

    public record CdcDeleteRequest(CdcRef cdcRef, boolean ifExists) {}

    @Override
    public CdcDeleteResult execute() {
        val status = cdcGateway.delete(request.cdcRef);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> handleCdcDeleted();
            case DeletionStatus.NotFound<?> _ -> handleCdcNotFound(request.cdcRef, request.ifExists);
        };
    }

    private CdcDeleteResult handleCdcDeleted() {
        return new CdcDeleted();
    }

    private CdcDeleteResult handleCdcNotFound(CdcRef cdcRef, boolean ifExists) {
        if (ifExists) {
            return new CdcNotFound();
        } else {
            return new CdcIllegallyNotFound();
        }
    }

}
