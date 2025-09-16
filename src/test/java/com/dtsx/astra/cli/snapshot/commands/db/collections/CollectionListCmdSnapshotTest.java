package com.dtsx.astra.cli.snapshot.commands.db.collections;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway.FoundKeyspaces;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForDifferentOutputs;
import com.dtsx.astra.cli.testlib.Fixtures.Collections;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CollectionListCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier foundWithKeyspace = (o) -> o
        .gateway(CollectionGateway.class, (mock) -> {
            when(mock.findAll(any())).thenReturn(Collections.Many);
        })
        .verify((mocks) -> {
            verify(mocks.collectionGateway()).findAll(Databases.Keyspace);
        });

    private final SnapshotTestOptionsModifier foundWithAll = (o) -> o
        .gateway(KeyspaceGateway.class, (mock) -> {
            when(mock.findAll(any())).thenReturn(new FoundKeyspaces("default_keyspace", List.of("ks1", "ks2")));
        })
        .gateway(CollectionGateway.class, (mock) -> {
            when(mock.findAll(any())).thenReturn(Collections.Many);
        })
        .verify((mocks) -> {
            verify(mocks.keyspaceGateway()).findAll(Databases.NameRef);
            verify(mocks.collectionGateway(), times(2)).findAll(any());
        });

    @TestForAllOutputs
    public void collections_found_using_keyspace(OutputType outputType) {
        verifyRun("db list-collections ${DatabaseName} -k ${Keyspace}", outputType, foundWithKeyspace);
    }

    @TestForDifferentOutputs
    public void collections_found_using_all(OutputType outputType) {
        verifyRun("db list-collections ${DatabaseName} --all", outputType, foundWithAll);
    }
}
