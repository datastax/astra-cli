package com.dtsx.astra.cli.commands.db.collections;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputJson;
import com.dtsx.astra.cli.core.output.table.RenderableShellTable;
import com.dtsx.astra.cli.core.output.table.ShellTable;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.collection.CollectionDescribeOperation;
import lombok.val;
import picocli.CommandLine.Command;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.COLLECTION_NOT_FOUND;
import static com.dtsx.astra.cli.operations.db.collection.CollectionDescribeOperation.*;

@Command(
    name = "describe-collection",
    description = "Describe an existing Data API collection with detailed information including vector configuration and indexing options"
)
@Example(
    comment = "Describe a collection in the default keyspace",
    command = "${cli.name} db describe-collection my_db -c my_collection"
)
@Example(
    comment = "Describe a collection in a specific keyspace",
    command = "${cli.name} db describe-collection my_db -k my_keyspace -c my_collection"
)
public class CollectionDescribeCmd extends AbstractPromptForCollectionCmd<CollectionDescribeResult> {
    @Override
    protected final OutputJson executeJson(Supplier<CollectionDescribeResult> result) {
            return switch (result.get()) {
            case CollectionNotFound() -> throwCollectionNotFound();
            case CollectionFound(var info) -> OutputJson.serializeValue(info.raw());
        };
    }

    @Override
    public final OutputAll execute(Supplier<CollectionDescribeResult> result) {
        return switch (result.get()) {
            case CollectionNotFound() -> throwCollectionNotFound();
            case CollectionFound(var info) -> handleCollectionFound(info);
        };
    }

    private OutputAll handleCollectionFound(CollectionInfo info) {
        return mkTable(info);
    }

    private RenderableShellTable mkTable(CollectionInfo info) {
        val attrs = new LinkedHashMap<String, Object>();

        attrs.put("Name", info.name());
        attrs.put("Estimated Count", String.valueOf(info.estimatedCount()));

        info.defaultIdType().ifPresent(idType -> 
            attrs.put("DefaultId", idType)
        );

        info.indexing().ifPresent(indexing -> {
            attrs.put("Indexing Allowed", indexing.allow().size() + " allowed");
            attrs.put("Indexing Denied", indexing.deny().size() + " denied");
        });

        info.vector().ifPresent(vector -> {
            attrs.put("Vector Dimension", String.valueOf(vector.dimension()));
            attrs.put("Similarity Metric", vector.metric());

            vector.vectorize().ifPresent(vectorize -> {
                attrs.put("AI Provider", vectorize.provider());
                attrs.put("AI Model", vectorize.modelName());
                
                if (!vectorize.authentication().isEmpty()) {
                    val authStr = vectorize.authentication().entrySet().stream()
                        .map(entry -> entry.getKey() + ": " + entry.getValue())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("--");
                    attrs.put("Authentication", authStr);
                } else {
                    attrs.put("Authentication", "--");
                }
                
                if (!vectorize.parameters().isEmpty()) {
                    val paramsStr = vectorize.parameters().entrySet().stream()
                        .map(entry -> entry.getKey() + ": " + entry.getValue())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("--");
                    attrs.put("Parameters", paramsStr);
                } else {
                    attrs.put("Parameters", "--");
                }
            });
        });
        
        return ShellTable.forAttributes(attrs);
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
                "${cli.name} db list-collections %s -k %s".formatted($keyspaceRef.db(), $keyspaceRef.name())),
            new Hint("Create the collection:",
                "${cli.name} db create-collection %s -k %s -c %s [...options]".formatted($keyspaceRef.db(), $keyspaceRef.name(), $collRef.name()))
        ));
    }

    @Override
    protected Operation<CollectionDescribeResult> mkOperation() {
        return new CollectionDescribeOperation(collectionGateway, new CollectionDescribeRequest($collRef));
    }

    @Override
    protected String collectionPrompt() {
        return "Select the collection to describe:";
    }
}
