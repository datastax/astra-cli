package com.dtsx.astra.cli.commands.db.table;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.table.RenderableShellTable;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.table.TableDescribeOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.ArrayList;
import java.util.Map;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.COLLECTION_NOT_FOUND;
import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.operations.db.table.TableDescribeOperation.*;
import static com.dtsx.astra.cli.utils.StringUtils.renderCommand;
import static com.dtsx.astra.cli.utils.StringUtils.renderComment;

@Command(
    name = "describe-table",
    description = "Describe an existing table with detailed information including columns and primary key structure"
)
@Example(
    comment = "Describe a table in the default keyspace",
    command = "astra db describe-table my_db -c my_table"
)
@Example(
    comment = "Describe a table in a specific keyspace",
    command = "astra db describe-table my_db -k my_keyspace -c my_table"
)
public class TableDescribeCmd extends AbstractTableSpecificCmd<TableDescribeResult> {
    @Override
    public final OutputAll execute(TableDescribeResult result) {
        return switch (result) {
            case TableNotFound() -> throwTableNotFound();
            case TableFound(var info) -> handleTableFound(info);
        };
    }

    private OutputAll handleTableFound(TableInfo info) {
        return mkTable(info);
    }

    private RenderableShellTable mkTable(TableInfo info) {
        val attrs = new ArrayList<Map<String, Object>>();

        attrs.add(ShellTable.attr("Name", info.name()));
        attrs.add(ShellTable.attr("", ""));
        attrs.add(ShellTable.attr(highlight("COLUMNS"), ""));

        info.columns().forEach((columnName, columnInfo) -> {
            attrs.add(ShellTable.attr(columnName, columnInfo.cqlDefinition()));
        });

        attrs.add(ShellTable.attr("", ""));
        attrs.add(ShellTable.attr(highlight("PRIMARY KEY"), ""));
        
        if (!info.primaryKey().partitionBy().isEmpty()) {
            val partitionKey = String.join(", ", info.primaryKey().partitionBy());
            attrs.add(ShellTable.attr("Partition Key", partitionKey));
        }
        
        if (!info.primaryKey().clusteringColumns().isEmpty()) {
            val clusteringColumns = String.join(", ", info.primaryKey().clusteringColumns());
            attrs.add(ShellTable.attr("Clustering Columns", clusteringColumns));
        }
        
        return new ShellTable(attrs).withAttributeColumns();
    }

    private OutputAll throwTableNotFound() {
        throw new AstraCliException(COLLECTION_NOT_FOUND, """
          @|bold,red Error: Table '%s' does not exist in keyspace '%s' of database '%s'.|@

          %s
          %s
        """.formatted(
            $tableRef.name(),
            $keyspaceRef.name(),
            $keyspaceRef.db(),
            renderComment("List existing tables:"),
            renderCommand("astra db list-tables %s -k %s".formatted($keyspaceRef.db(), $keyspaceRef.name()))
        ));
    }

    @Override
    protected Operation<TableDescribeResult> mkOperation() {
        return new TableDescribeOperation(tableGateway, new TableDescribeRequest($tableRef));
    }
}
