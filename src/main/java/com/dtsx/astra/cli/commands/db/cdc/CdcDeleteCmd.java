package com.dtsx.astra.cli.commands.db.cdc;

import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.core.models.CdcId;
import com.dtsx.astra.cli.core.models.CdcRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.cdc.CdcDeleteOperation;
import com.dtsx.astra.cli.operations.db.cdc.CdcDeleteOperation.CdcDeleted;
import com.dtsx.astra.cli.operations.db.cdc.CdcDeleteOperation.CdcNotFound;
import lombok.val;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "delete-cdc"
)
public final class CdcDeleteCmd extends AbstractCdcCmd {
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
    protected OutputAll execute() {
        CdcRef cdcRef;

        if (cdcIdentifier.cdcId != null) {
            val cdcId = CdcId.parse(cdcIdentifier.cdcId).getRight((msg) -> {
                throw new OptionValidationException("cdc id", msg);
            });
            cdcRef = CdcRef.fromId(dbRef, cdcId);
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
            cdcRef = CdcRef.fromDefinition(tableRef, tenantName);
        }

        val result = new CdcDeleteOperation(cdcGateway).execute(cdcRef, ifExists);

        return switch (result) {
            case CdcNotFound() -> {
                yield OutputAll.message("CDC " + highlight(cdcRef.toString()) + " does not exist; nothing to delete");
            }
            case CdcDeleted() -> {
                yield OutputAll.message("CDC " + highlight(cdcRef.toString()) + " has been deleted");
            }
        };
    }
}