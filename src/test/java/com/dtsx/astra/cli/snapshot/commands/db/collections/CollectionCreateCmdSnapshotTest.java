package com.dtsx.astra.cli.snapshot.commands.db.collections;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.models.CollectionRef;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Collections;

import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CollectionCreateCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier createCollection(Function<CollectionRef, CreationStatus<CollectionRef>> lift) {
        return (o) -> o
            .gateway(CollectionGateway.class, (mock) -> {
                doReturn(lift.apply(Collections.Ref)).when(mock).create(any(), any(), any(), any(), any(), any(), any(), any(), any());
            })
            .verify((mocks) -> {
                verify(mocks.collectionGateway()).create(eq(Collections.Ref), any(), any(), any(), any(), any(), any(), any(), any());
            });
    }

    @TestForAllOutputs
    public void collection_created(OutputType outputType) {
        verifyRun("db create-collection ${DatabaseName} -c ${CollectionName}", outputType, createCollection(CreationStatus::created));
    }

    @TestForDifferentOutputs
    public void error_collection_already_exists(OutputType outputType) {
        verifyRun("db create-collection ${DatabaseName} -c ${CollectionName}", outputType, createCollection(CreationStatus::alreadyExists));
    }

    @TestForDifferentOutputs
    public void allow_collection_already_exists(OutputType outputType) {
        verifyRun("db create-collection ${DatabaseName} -c ${CollectionName} --if-not-exists", outputType, createCollection(CreationStatus::alreadyExists));
    }
}
