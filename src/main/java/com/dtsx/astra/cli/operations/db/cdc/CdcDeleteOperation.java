package com.dtsx.astra.cli.operations.db.cdc;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.models.CdcRef;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.cdc.CdcGateway;
import lombok.RequiredArgsConstructor;
import lombok.val;

@RequiredArgsConstructor
public class CdcDeleteOperation {
    private final CdcGateway cdcGateway;

    public sealed interface CdcDeleteResult {}
    public record CdcNotFound() implements CdcDeleteResult {}
    public record CdcDeleted() implements CdcDeleteResult {}

    public CdcDeleteResult execute(CdcRef cdcRef, boolean ifExists) {
        val status = cdcGateway.delete(cdcRef);

        return switch (status) {
            case DeletionStatus.Deleted<?> _ -> handleCdcDeleted();
            case DeletionStatus.NotFound<?> _ -> handleCdcNotFound(cdcRef, ifExists);
        };
    }

    private CdcDeleteResult handleCdcDeleted() {
        return new CdcDeleted();
    }

    private CdcDeleteResult handleCdcNotFound(CdcRef cdcRef, boolean ifExists) {
        if (ifExists) {
            return new CdcNotFound();
        } else {
            throw new CdcNotFoundException(cdcRef);
        }
    }

    public static class CdcNotFoundException extends AstraCliException {
        public CdcNotFoundException(CdcRef cdcRef) {
            super("""
              @|bold,red Error: Cdc '%s' does not exist in database '%s'.|@
            
              This may be expected, but to avoid this error:
              - Run %s to see the existing cdcs.
              - Pass the %s flag to skip this error if the cdc doesn't exist.
            """.formatted(
                cdcRef,
                cdcRef.db(),
                AstraColors.highlight("astra db list-cdcs " + cdcRef.db()),
                AstraColors.highlight("--if-exists")
            ));
        }
    }
}
