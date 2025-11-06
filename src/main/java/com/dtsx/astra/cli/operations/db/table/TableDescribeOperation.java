package com.dtsx.astra.cli.operations.db.table;

import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnDefinition;
import com.datastax.astra.client.tables.definition.columns.TableColumnTypes;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;
import com.dtsx.astra.cli.operations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.operations.db.table.TableDescribeOperation.TableDescribeResult;

@RequiredArgsConstructor
public class TableDescribeOperation implements Operation<TableDescribeResult> {
    private final TableGateway tableGateway;
    private final TableDescribeRequest request;

    public sealed interface TableDescribeResult {}
    public record TableNotFound() implements TableDescribeResult {}
    public record TableFound(TableInfo tableInfo) implements TableDescribeResult {}

    public record TableDescribeRequest(TableRef tableRef) {}

    public record TableInfo(
        String name,
        Map<String, ColumnInfo> columns,
        PrimaryKeyInfo primaryKey,
        TableDefinition raw
    ) {}

    public record ColumnInfo(
        String name,
        String type,
        String cqlDefinition
    ) {}

    public record PrimaryKeyInfo(
        List<String> partitionBy,
        List<String> clusteringColumns
    ) {}

    @Override
    public TableDescribeResult execute() {
        val tableResult = tableGateway.findOne(request.tableRef);
        
        if (tableResult.isEmpty()) {
            return new TableNotFound();
        }
        
        val definition = tableResult.get();
        
        return new TableFound(new TableInfo(
            request.tableRef.name(),
            extractColumnInfo(definition),
            extractPrimaryKeyInfo(definition),
            definition
        ));
    }

    private Map<String, ColumnInfo> extractColumnInfo(TableDefinition definition) {
        return definition.getColumns().entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> {
                    val columnDef = entry.getValue();
                    val type = columnDef.getType();
                    val cqlDefinition = getCqlDefinition(columnDef);
                    
                    return new ColumnInfo(
                        entry.getKey(),
                        type.name(),
                        cqlDefinition
                    );
                }
            ));
    }

    private String getCqlDefinition(TableColumnDefinition columnDef) {
        val type = columnDef.getType();
        
        if (TableColumnTypes.LIST.equals(type) ||
            TableColumnTypes.SET.equals(type) ||
            TableColumnTypes.VECTOR.equals(type) ||
            TableColumnTypes.MAP.equals(type)) {
            
            if (columnDef.getApiSupport() != null) {
                return columnDef.getApiSupport().getCqlDefinition().toUpperCase();
            } else {
                return type + "<?>";
            }
        }
        
        return type.name();
    }

    private PrimaryKeyInfo extractPrimaryKeyInfo(TableDefinition definition) {
        val primaryKey = definition.getPrimaryKey();
        
        val clusteringColumns = primaryKey.getPartitionSort().entrySet()
            .stream()
            .map(entry -> entry.getKey() + "(" + (entry.getValue() == 1 ? "ASC" : "DESC") + ")")
            .collect(Collectors.toList());
        
        return new PrimaryKeyInfo(
            primaryKey.getPartitionBy(),
            clusteringColumns
        );
    }
}
