package com.dtsx.astra.cli.snapshot.commands.db.collections;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class CollectionTruncateCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier collectionTruncated = (o) -> o
        .gateway(CollectionGateway.class, (mock) -> {
            doReturn(DeletionStatus.deleted(Collections.Ref.name())).when(mock).truncate(any());
        })
        .verify((mocks) -> {
            verify(mocks.collectionGateway()).truncate(Collections.Ref);
        });

    private final SnapshotTestOptionsModifier collectionNotFound = (o) -> o
        .gateway(CollectionGateway.class, (mock) -> {
            doReturn(DeletionStatus.notFound(Collections.Ref.name())).when(mock).truncate(any());
        })
        .verify((mocks) -> {
            verify(mocks.collectionGateway()).truncate(Collections.Ref);
        });

    @TestForAllOutputs
    public void collection_truncated(OutputType outputType) {
        verifyRun("db truncate-collection ${DatabaseName} -k default_keyspace -c ${CollectionName} --verbose", outputType, collectionTruncated);
    }

    @TestForDifferentOutputs
    public void error_collection_not_found(OutputType outputType) {
        verifyRun("db truncate-collection ${DatabaseName} -k default_keyspace -c ${CollectionName}", outputType, collectionNotFound);
    }
}
