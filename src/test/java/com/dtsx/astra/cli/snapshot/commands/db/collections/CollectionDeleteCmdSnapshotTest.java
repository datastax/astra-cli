package com.dtsx.astra.cli.snapshot.commands.db.collections;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
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

public class CollectionDeleteCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier deleteCollection(Function<CollectionRef, DeletionStatus<CollectionRef>> lift) {
        return (o) -> o
            .gateway(CollectionGateway.class, (mock) -> {
                doReturn(lift.apply(Collections.Ref)).when(mock).delete(any());
            })
            .verify((mocks) -> {
                verify(mocks.collectionGateway()).delete(Collections.Ref);
            });
    }

    @TestForAllOutputs
    public void collection_deleted(OutputType outputType) {
        verifyRun("db delete-collection ${DatabaseName} -k default_keyspace -c ${CollectionName}", outputType, deleteCollection(DeletionStatus::deleted));
    }

    @TestForDifferentOutputs
    public void error_collection_not_found(OutputType outputType) {
        verifyRun("db delete-collection ${DatabaseName} -k default_keyspace -c ${CollectionName}", outputType, deleteCollection(DeletionStatus::notFound));
    }

    @TestForDifferentOutputs
    public void allow_collection_not_found(OutputType outputType) {
        verifyRun("db delete-collection ${DatabaseName} -k default_keyspace -c ${CollectionName} --if-exists", outputType, deleteCollection(DeletionStatus::notFound));
    }
}
