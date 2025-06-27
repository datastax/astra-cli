package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.exceptions.cli.OptionValidationException;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Option;

public abstract class AbstractTableSpecificCmd<OpRes> extends AbstractTableCmd<OpRes> {
    protected TableRef $tableRef;

    @Option(
        names = { "--table", "-t" },
        description = { "The table to use", DEFAULT_VALUE },
        paramLabel = "TABLE",
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
