package com.dtsx.astra.cli.commands.db.cdc;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.core.models.CdcId;
import com.dtsx.astra.cli.core.models.CdcRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cdc.CdcDeleteOperation;
import lombok.val;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.cdc.CdcDeleteOperation.*;

@Command(
    name = "delete-cdc"
)
public class CdcDeleteCmd extends AbstractCdcCmd<CdcDeleteResult> {
    @ArgGroup(exclusive = true, multiplicity = "1")
    CdcIdentifier cdcIdentifier;

    static class CdcIdentifier {
        @Option(
            names = { "--cdc-id" },
            description = { "The CDC ID to delete", DEFAULT_VALUE },
            paramLabel = "CDC_ID",
            required = true
        )
        public String cdcId;

        @ArgGroup(exclusive = false)
        TableTenant tableTenant;

        static class TableTenant {
            @Option(
                names = { "--keyspace", "-k" },
                description = { "The keyspace", DEFAULT_VALUE },
                paramLabel = "KEYSPACE",
                required = true
            )
            public String keyspace;

            @Option(
                names = { "--table", "-t" },
                description = { "The table", DEFAULT_VALUE },
                paramLabel = "TABLE",  
                required = true
            )
            public String table;

            @Option(
                names = { "--tenant" },
                description = { "The tenant name", DEFAULT_VALUE },
                paramLabel = "TENANT",
                required = true
            )
            public String tenant;
        }
    }

    @Option(
        names = { "--if-exists" },
        description = { "Do not fail if CDC does not exist", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean ifExists;

    @Override
    public final OutputAll execute(CdcDeleteResult result) {
        val cdcRef = getCdcRef();
        
        val message = switch (result) {
            case CdcNotFound() -> "CDC " + highlight(cdcRef.toString()) + " does not exist; nothing to delete";
            case CdcIllegallyNotFound() -> throw new CdcNotFoundException(cdcRef);
            case CdcDeleted() -> "CDC " + highlight(cdcRef.toString()) + " has been deleted";
        };
        
        return OutputAll.message(message);
    }

    @Override
    protected Operation<CdcDeleteResult> mkOperation() {
        return new CdcDeleteOperation(cdcGateway, new CdcDeleteRequest(getCdcRef(), ifExists));
    }

    private CdcRef getCdcRef() {
        if (cdcIdentifier.cdcId != null) {
            val cdcId = CdcId.parse(cdcIdentifier.cdcId).getRight((msg) -> {
                throw new OptionValidationException("cdc id", msg);
            });
            return CdcRef.fromId(dbRef, cdcId);
        } else {
            val tableTenant = cdcIdentifier.tableTenant;
            val keyspaceRef = KeyspaceRef.parse(dbRef, tableTenant.keyspace).getRight((msg) -> {
                throw new OptionValidationException("keyspace", msg);
            });
            val tableRef = TableRef.parse(keyspaceRef, tableTenant.table).getRight((msg) -> {
                throw new OptionValidationException("table", msg);
            });
            val tenantName = TenantName.parse(tableTenant.tenant).getRight((msg) -> {
                throw new OptionValidationException("tenant", msg);
            });
            return CdcRef.fromDefinition(tableRef, tenantName);
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
