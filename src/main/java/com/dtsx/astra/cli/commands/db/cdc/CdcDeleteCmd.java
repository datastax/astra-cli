package com.dtsx.astra.cli.commands.db.cdc;

import com.dtsx.astra.cli.core.CliConstants.$Keyspace;
import com.dtsx.astra.cli.core.CliConstants.$Table;
import com.dtsx.astra.cli.core.CliConstants.$Tenant;
import com.dtsx.astra.cli.core.completions.impls.TenantNamesCompletion;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.models.*;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.cdc.CdcDeleteOperation;
import lombok.val;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.CDC_NOT_FOUND;
import static com.dtsx.astra.cli.core.output.ExitCode.UNSUPPORTED_EXECUTION;
import static com.dtsx.astra.cli.operations.db.cdc.CdcDeleteOperation.*;
import static com.dtsx.astra.cli.utils.MapUtils.sequencedMapOf;

@Command(
    name = "delete-cdc",
    description = "Delete a CDC (Change Data Capture) from the database"
)
@Example(
    comment = "Delete a CDC by ID",
    command = "${cli.name} db delete-cdc my_db --cdc-id abc123"
)
@Example(
    comment = "Delete a CDC by table and tenant",
    command = "${cli.name} db delete-cdc my_db -k my_keyspace -t my_table --tenant my_tenant"
)
@Example(
    comment = "Delete a CDC without failing if it doesn't exist",
    command = "${cli.name} db delete-cdc my_db --cdc-id abc123 --if-exists"
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
        public TableTenant tableTenant;

        static class TableTenant {
            @Option(
                names = { $Keyspace.LONG, $Keyspace.SHORT },
                description = "The keyspace",
                paramLabel = $Keyspace.LABEL,
                required = true
            )
            public String keyspace;

            @Option(
                names = { $Table.LONG },
                description =  "The table",
                paramLabel = $Table.LABEL,
                required = true
            )
            public String table;

            @Option(
                names = { $Tenant.LONG },
                description = "The tenant name",
                completionCandidates = TenantNamesCompletion.class,
                paramLabel = $Tenant.LABEL,
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
    protected void prelude() {
        super.prelude();
        throw new AstraCliException(UNSUPPORTED_EXECUTION, "Deleting CDCs via the Astra CLI is not currently not supported due to a dependency bug.");
    }

    @Override
    public final OutputAll execute(Supplier<CdcDeleteResult> result) {
        return switch (result.get()) {
            case CdcNotFound() -> handleCdcNotFound();
            case CdcIllegallyNotFound() -> throwCdcNotFound();
            case CdcDeleted() -> handleCdcDeleted();
        };
    }

    private OutputAll handleCdcNotFound() {
        val msg = """
          CDC %s does not exist in database %s; nothing to delete.
        """.formatted(
            ctx.highlight(cdcRef()),
            ctx.highlight($dbRef)
        );

        return OutputAll.response(msg, mkData(false), List.of(
            new Hint("List existing CDCs", "${cli.name} db list-cdcs " + $dbRef)
        ));
    }

    private OutputAll handleCdcDeleted() {
        val msg = """
          CDC %s has been deleted from database %s.
        """.formatted(
            ctx.highlight(cdcRef()),
            ctx.highlight($dbRef)
        );

        return OutputAll.response(msg, mkData(true), List.of(
            new Hint("List existing CDCs", "${cli.name} db list-cdcs " + $dbRef)
        ));
    }

    private <T> T throwCdcNotFound() {
        throw new AstraCliException(CDC_NOT_FOUND, """
          @|bold,red Error: CDC '%s' does not exist in database '%s'.|@

          To ignore this error, provide the @!--if-exists!@ flag to skip this error if the CDC doesn't exist.
        """.formatted(
            cdcRef(),
            $dbRef
        ), List.of(
            new Hint("Example fix", originalArgs(), "--if-exists"),
            new Hint("List existing CDCs", "${cli.name} db list-cdcs " + $dbRef)
        ));
    }

    private LinkedHashMap<String, Object> mkData(Boolean wasDeleted) {
        return sequencedMapOf(
            "wasDeleted", wasDeleted
        );
    }

    @Override
    protected Operation<CdcDeleteResult> mkOperation() {
        return new CdcDeleteOperation(cdcGateway, new CdcDeleteRequest(cdcRef(), $ifExists));
    }

    private CdcRef cdcRef() {
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
