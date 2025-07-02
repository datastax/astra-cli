package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.output.Hint;
import com.dtsx.astra.cli.core.output.output.OutputAll;
import com.dtsx.astra.cli.core.output.output.OutputJson;
import com.dtsx.astra.cli.core.output.table.RenderableShellTable;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.collection.CollectionDescribeOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.dtsx.astra.cli.core.exceptions.CliExceptionCode.COLLECTION_NOT_FOUND;
import static com.dtsx.astra.cli.operations.db.collection.CollectionDescribeOperation.*;

@Command(
    name = "describe-collection",
    description = "Describe an existing Data API collection with detailed information including vector configuration and indexing options"
)
@Example(
    comment = "Describe a collection in the default keyspace",
    command = "astra db describe-collection my_db -c my_collection"
)
@Example(
    comment = "Describe a collection in a specific keyspace",
    command = "astra db describe-collection my_db -k my_keyspace -c my_collection"
)
public class CollectionDescribeCmd extends AbstractCollectionSpecificCmd<CollectionDescribeResult> {
    @Override
    protected OutputJson executeJson(CollectionDescribeResult result) {
        return switch (result) {
            case CollectionNotFound() -> throwCollectionNotFound();
            case CollectionFound(var info) -> OutputJson.serializeValue(info.raw());
        };
    }

    @Override
    public final OutputAll execute(CollectionDescribeResult result) {
        return switch (result) {
            case CollectionNotFound() -> throwCollectionNotFound();
            case CollectionFound(var info) -> handleCollectionFound(info);
        };
    }

    private OutputAll handleCollectionFound(CollectionInfo info) {
        return mkTable(info);
    }

    private RenderableShellTable mkTable(CollectionInfo info) {
        val attrs = new ArrayList<Map<String, Object>>();

        attrs.add(ShellTable.attr("Name", info.name()));
        attrs.add(ShellTable.attr("Estimated Count", String.valueOf(info.estimatedCount())));

        info.defaultIdType().ifPresent(idType -> 
            attrs.add(ShellTable.attr("DefaultId", idType))
        );

        info.indexing().ifPresent(indexing -> {
            attrs.add(ShellTable.attr("Indexing Allowed", indexing.allow().size() + " allowed"));
            attrs.add(ShellTable.attr("Indexing Denied", indexing.deny().size() + " denied"));
        });

        info.vector().ifPresent(vector -> {
            attrs.add(ShellTable.attr("Vector Dimension", String.valueOf(vector.dimension())));
            attrs.add(ShellTable.attr("Similarity Metric", vector.metric()));

            vector.vectorize().ifPresent(vectorize -> {
                attrs.add(ShellTable.attr("AI Provider", vectorize.provider()));
                attrs.add(ShellTable.attr("AI Model", vectorize.modelName()));
                
                if (!vectorize.authentication().isEmpty()) {
                    val authStr = vectorize.authentication().entrySet().stream()
                        .map(entry -> entry.getKey() + ": " + entry.getValue())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("--");
                    attrs.add(ShellTable.attr("Authentication", authStr));
                } else {
                    attrs.add(ShellTable.attr("Authentication", "--"));
                }
                
                if (!vectorize.parameters().isEmpty()) {
                    val paramsStr = vectorize.parameters().entrySet().stream()
                        .map(entry -> entry.getKey() + ": " + entry.getValue())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("--");
                    attrs.add(ShellTable.attr("Parameters", paramsStr));
                } else {
                    attrs.add(ShellTable.attr("Parameters", "--"));
                }
            });
        });
        
        return new ShellTable(attrs).withAttributeColumns();
    }

    private <T> T throwCollectionNotFound() {
        throw new AstraCliException(COLLECTION_NOT_FOUND, """
          @|bold,red Error: Collection '%s' does not exist in keyspace '%s' of database '%s'.|@
        """.formatted(
            $collRef.name(),
            $keyspaceRef.name(),
            $keyspaceRef.db()
        ), List.of(
            new Hint("List existing collections:",
                "astra db list-collections %s -k %s".formatted($keyspaceRef.db(), $keyspaceRef.name())),
            new Hint("Create the collection:",
                "astra db create-collection %s -k %s -c %s [...options]".formatted($keyspaceRef.db(), $keyspaceRef.name(), $collRef.name()))
        ));
    }

    @Override
    protected Operation<CollectionDescribeResult> mkOperation() {
        return new CollectionDescribeOperation(collectionGateway, new CollectionDescribeRequest($collRef));
    }
}
