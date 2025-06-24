package com.dtsx.astra.cli.commands.db.cdc;

import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.db.cdc.CdcCreateOperation;
import com.dtsx.astra.cli.operations.db.cdc.CdcCreateOperation.CdcAlreadyExists;
import com.dtsx.astra.cli.operations.db.cdc.CdcCreateOperation.CdcCreated;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;

@Command(
    name = "create-cdc"
)
public final class CdcCreateCmd extends AbstractCdcCmd {
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
    protected OutputAll execute() {
        val keyspaceRef = KeyspaceRef.parse(dbRef, keyspaceOption).getRight((msg) -> {
            throw new OptionValidationException("keyspace", msg);
        });

        val tableRef = TableRef.parse(keyspaceRef, tableOption).getRight((msg) -> {
            throw new OptionValidationException("table", msg);
        });

        val tenantName = TenantName.parse(tenantOption).getRight((msg) -> {
            throw new OptionValidationException("tenant name", msg);
        });

        val result = new CdcCreateOperation(cdcGateway).execute(tableRef, tenantName, topicPartitions, ifNotExists);

        return switch (result) {
            case CdcAlreadyExists() -> {
                yield OutputAll.message("CDC already exists for table " + highlight(tableRef.toString()) + " with tenant " + highlight(tenantName.toString()));
            }
            case CdcCreated() -> {
                yield OutputAll.message("CDC has been created for table " + highlight(tableRef.toString()) + " with tenant " + highlight(tenantName.toString()));
            }
        };
    }
}