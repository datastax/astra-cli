package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.core.CliConstants.$Table;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.models.TableRef;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Option;

public abstract class AbstractTableSpecificCmd<OpRes> extends AbstractTableCmd<OpRes> {
    protected TableRef $tableRef;

    @Option(
        names = { $Table.LONG, $Table.SHORT },
        description = "The table to use",
        paramLabel = $Table.LABEL,
        required = true
    )
    private String actualTableRefOption;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();

        this.$tableRef = TableRef.parse($keyspaceRef, actualTableRefOption).getRight((msg) -> {
            throw new OptionValidationException("table name", msg);
        });
    }
}
