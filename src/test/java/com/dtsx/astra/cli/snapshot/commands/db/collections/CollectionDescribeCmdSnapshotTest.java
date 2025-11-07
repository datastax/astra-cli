package com.dtsx.astra.cli.snapshot.commands.db.collections;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Collections;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CollectionDescribeCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier collectionFound = (o) -> o
        .gateway(CollectionGateway.class, (mock) -> {
            when(mock.findOne(any())).thenReturn(Optional.of(Collections.One));
            when(mock.estimatedDocumentCount(any())).thenReturn(12345L);
        })
        .verify((mocks) -> {
            verify(mocks.collectionGateway()).findOne(Collections.Ref);
            verify(mocks.collectionGateway()).estimatedDocumentCount(Collections.Ref);
        });

    private final SnapshotTestOptionsModifier collectionNotFound = (o) -> o
        .gateway(CollectionGateway.class, (mock) -> {
            when(mock.findOne(any())).thenReturn(Optional.empty());
        })
        .verify((mocks) -> {
            verify(mocks.collectionGateway()).findOne(Collections.Ref);
            verify(mocks.collectionGateway(), never()).estimatedDocumentCount(any());
        });

    @TestForAllOutputs
    public void collection_found(OutputType outputType) {
        verifyRun("db describe-collection ${DatabaseName} -k default_keyspace -c ${CollectionName}", outputType, collectionFound);
    }

    @TestForDifferentOutputs
    public void error_collection_not_found(OutputType outputType) {
        verifyRun("db describe-collection ${DatabaseName} -k default_keyspace -c ${CollectionName}", outputType, collectionNotFound);
    }
}

