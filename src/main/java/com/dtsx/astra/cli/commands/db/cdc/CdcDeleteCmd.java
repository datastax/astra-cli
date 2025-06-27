package com.dtsx.astra.cli.commands.db.cdc;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.CdcId;
import com.dtsx.astra.cli.core.models.CdcRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cdc.CdcDeleteOperation;
import lombok.val;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.CDC_NOT_FOUND;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.cdc.CdcDeleteOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@Command(
    name = "delete-cdc",
    description = "Delete a CDC (Change Data Capture) from the database"
)
@Example(
    comment = "Delete a CDC by ID",
    command = "astra db delete-cdc my_db --cdc-id abc123"
)
@Example(
    comment = "Delete a CDC by table and tenant",
    command = "astra db delete-cdc my_db -k my_keyspace -t my_table --tenant my_tenant"
)
@Example(
    comment = "Delete a CDC without failing if it doesn't exist",
    command = "astra db delete-cdc my_db --cdc-id abc123 --if-exists"
)
public class CdcDeleteCmd extends AbstractCdcCmd<CdcDeleteResult> {
    @ArgGroup(multiplicity = "1")
    CdcIdentifier $cdcIdentifier;

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
    public boolean $ifExists;

    @Override
    public final OutputAll execute(CdcDeleteResult result) {
        val message = switch (result) {
            case CdcNotFound() -> handleCdcNotFound();
            case CdcIllegallyNotFound() -> throwCdcNotFound();
            case CdcDeleted() -> handleCdcDeleted();
        };
        
        return OutputAll.message(trimIndent(message));
    }

    private String handleCdcNotFound() {
        val cdcRef = getCdcRef();
        return """
          CDC %s does not exist in database %s; nothing to delete.

          %s
          %s
        """.formatted(
            highlight(cdcRef.toString()),
            highlight($dbRef),
            renderComment("List existing CDCs:"),
            renderCommand("astra db list-cdcs %s".formatted($dbRef))
        );
    }

    private String handleCdcDeleted() {
        val cdcRef = getCdcRef();
        return """
          CDC %s has been deleted from database %s.
        """.formatted(
            highlight(cdcRef.toString()),
            highlight($dbRef)
        );
    }

    private String throwCdcNotFound() {
        val cdcRef = getCdcRef();
        
        throw new AstraCliException(CDC_NOT_FOUND, """
          @|bold,red Error: CDC '%s' does not exist in database '%s'.|@

          To ignore this error, provide the %s flag to skip this error if the CDC doesn't exist.

          %s
          %s

          %s
          %s
        """.formatted(
            cdcRef.toString(),
            $dbRef,
            highlight("--if-exists"),
            renderComment("Example fix:"),
            renderCommand(originalArgs(), "--if-exists"),
            renderComment("List existing CDCs:"),
            renderCommand("astra db list-cdcs %s".formatted($dbRef))
        ));
    }

    @Override
    protected Operation<CdcDeleteResult> mkOperation() {
        return new CdcDeleteOperation(cdcGateway, new CdcDeleteRequest(getCdcRef(), $ifExists));
    }

    private CdcRef getCdcRef() {
        if ($cdcIdentifier.cdcId != null) {
            val cdcId = CdcId.parse($cdcIdentifier.cdcId).getRight((msg) -> {
                throw new OptionValidationException("cdc id", msg);
            });
            return CdcRef.fromId($dbRef, cdcId);
        } else {
            val keyspaceRef = KeyspaceRef.parse($dbRef, $cdcIdentifier.tableTenant.keyspace).getRight((msg) -> {
                throw new OptionValidationException("keyspace", msg);
            });
            val tableRef = TableRef.parse(keyspaceRef, $cdcIdentifier.tableTenant.table).getRight((msg) -> {
                throw new OptionValidationException("table", msg);
            });
            val tenantName = TenantName.parse($cdcIdentifier.tableTenant.tenant).getRight((msg) -> {
                throw new OptionValidationException("tenant", msg);
            });
            return CdcRef.fromDefinition(tableRef, tenantName);
        }
    }
}
