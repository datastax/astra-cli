package com.dtsx.astra.cli.commands.db.cdc;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.core.models.CdcRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cdc.CdcCreateOperation;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.cdc.CdcCreateOperation.*;

@Command(
    name = "create-cdc"
)
public class CdcCreateCmd extends AbstractCdcCmd<CdcCreateResult> {
    @Option(
        names = { "--keyspace", "-k" },
        description = { "The keyspace", DEFAULT_VALUE },
        paramLabel = "KEYSPACE",
        defaultValue = "default_keyspace"
    )
    public String keyspaceOption;

    @Option(
        names = { "--table", "-t" },
        description = { "The table to create CDC for", DEFAULT_VALUE },
        paramLabel = "TABLE",
        required = true
    )
    public String tableOption;

    @Option(
        names = { "--tenant" },
        description = { "The tenant name", DEFAULT_VALUE },
        paramLabel = "TENANT",
        required = true
    )
    public String tenantOption;

    @Option(
        names = { "--topic-partitions" },
        description = { "Number of topic partitions", DEFAULT_VALUE },
        paramLabel = "PARTITIONS",
        defaultValue = "3"
    )
    public int topicPartitions;

    @Option(
        names = { "--if-not-exists" },
        description = { "Will create a new CDC only if none exists", DEFAULT_VALUE },
        defaultValue = "false"
    )
    public boolean ifNotExists;

    @Override
    public final OutputAll execute(CdcCreateResult result) {
        val tableRef = getTableRef();
        val tenantName = getTenantName();
        
        val message = switch (result) {
            case CdcAlreadyExists() -> "CDC already exists for table " + highlight(tableRef.toString()) + " with tenant " + highlight(tenantName.toString());
            case CdcIllegallyAlreadyExists() -> throw new CdcAlreadyExistsException(CdcRef.fromDefinition(tableRef, tenantName));
            case CdcCreated() -> "CDC has been created for table " + highlight(tableRef.toString()) + " with tenant " + highlight(tenantName.toString());
        };
        
        return OutputAll.message(message);
    }

    @Override
    protected Operation<CdcCreateResult> mkOperation() {
        return new CdcCreateOperation(cdcGateway, new CdcCreateRequest(
            getTableRef(),
            getTenantName(),
            topicPartitions,
            ifNotExists
        ));
    }

    private TableRef getTableRef() {
        val keyspaceRef = KeyspaceRef.parse(dbRef, keyspaceOption).getRight((msg) -> {
            throw new OptionValidationException("keyspace", msg);
        });

        return TableRef.parse(keyspaceRef, tableOption).getRight((msg) -> {
            throw new OptionValidationException("table", msg);
        });
    }

    private TenantName getTenantName() {
        return TenantName.parse(tenantOption).getRight((msg) -> {
            throw new OptionValidationException("tenant name", msg);
        });
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
