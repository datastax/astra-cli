package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.core.CliConstants.$Table;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.output.prompters.specific.TableNamePrompter;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Option;

public abstract class AbstractPromptForTableCmd<OpRes> extends AbstractTableCmd<OpRes> {
    protected TableRef $tableRef;

    @Option(
        names = { $Table.LONG, $Table.SHORT },
        description = "The table to use",
        paramLabel = $Table.LABEL
    )
    private String tableName;

    @Override
    @MustBeInvokedByOverriders
    protected void prelude() {
        super.prelude();

        if (tableName == null) {
            tableName = TableNamePrompter.prompt(ctx, tableGateway, $keyspaceRef, tablePrompt(), (b) -> b.fallbackFlag("-t").fix(originalArgs(), "-t <table>"));
        }

        this.$tableRef = TableRef.parse($keyspaceRef, tableName).getRight((msg) -> {
            throw new OptionValidationException("table name", msg);
        });
    }
    protected abstract String tablePrompt();
}
