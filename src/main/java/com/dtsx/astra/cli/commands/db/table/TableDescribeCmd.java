package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.RenderableShellTable;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.table.TableDescribeOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.COLLECTION_NOT_FOUND;
import static com.dtsx.astra.cli.operations.db.table.TableDescribeOperation.*;

@Command(
    name = "describe-table",
    description = "Describe an existing table with detailed information including columns and primary key structure"
)
@Example(
    comment = "Describe a table in the default keyspace",
    command = "${cli.name} db describe-table my_db -c my_table"
)
@Example(
    comment = "Describe a table in a specific keyspace",
    command = "${cli.name} db describe-table my_db -k my_keyspace -c my_table"
)
public class TableDescribeCmd extends AbstractTableSpecificCmd<TableDescribeResult> {
    @Override
    protected final OutputJson executeJson(Supplier<TableDescribeResult> result) {
        return switch (result.get()) {
            case TableNotFound() -> throwTableNotFound();
            case TableFound(var info) -> OutputJson.serializeValue(info.raw());
        };
    }

    @Override
    public final OutputAll execute(Supplier<TableDescribeResult> result) {
        return switch (result.get()) {
            case TableNotFound() -> throwTableNotFound();
            case TableFound(var info) -> handleTableFound(info);
        };
    }

    private OutputAll handleTableFound(TableInfo info) {
        return mkTable(info);
    }

    private RenderableShellTable mkTable(TableInfo info) {
        val attrs = new LinkedHashMap<String, Object>();

        attrs.put("Name", info.name());
        attrs.put("", "");
        attrs.put(ctx.highlight("COLUMNS"), "");

        info.columns().forEach((columnName, columnInfo) -> {
            attrs.put(columnName, columnInfo.cqlDefinition());
        });

        attrs.put("", "");
        attrs.put(ctx.highlight("PRIMARY KEY"), "");
        
        if (!info.primaryKey().partitionBy().isEmpty()) {
            val partitionKey = String.join(", ", info.primaryKey().partitionBy());
            attrs.put("Partition Key", partitionKey);
        }
        
        if (!info.primaryKey().clusteringColumns().isEmpty()) {
            val clusteringColumns = String.join(", ", info.primaryKey().clusteringColumns());
            attrs.put("Clustering Columns", clusteringColumns);
        }
        
        return ShellTable.forAttributes(attrs);
    }

    private <T> T throwTableNotFound() {
        throw new AstraCliException(COLLECTION_NOT_FOUND, """
          @|bold,red Error: Table '%s' does not exist in keyspace '%s' of database '%s'.|@
        """.formatted(
            $tableRef.name(),
            $keyspaceRef.name(),
            $keyspaceRef.db()
        ), List.of(
            new Hint("List existing tables:", "${cli.name} db list-tables %s -k %s".formatted($keyspaceRef.db(), $keyspaceRef.name()))
        ));
    }

    @Override
    protected Operation<TableDescribeResult> mkOperation() {
        return new TableDescribeOperation(tableGateway, new TableDescribeRequest($tableRef));
    }
}
