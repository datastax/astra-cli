package com.dtsx.astra.cli.snapshot.commands.db.misc;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.testlib.Fixtures;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EmbeddingProvidersListCmdSnapshotTest extends BaseCmdSnapshotTest {
    public SnapshotTestOptionsModifier opts = o -> o
        .gateway(DbGateway.class, (mock) -> {
            when(mock.findEmbeddingProviders(any())).thenReturn(Fixtures.FindEmbeddingProvidersResult);
        })
        .verify((mocks) -> {
            verify(mocks.dbGateway()).findEmbeddingProviders(Databases.NameRef);
        });

    @TestForAllOutputs
    public void list_embedding_providers(OutputType outputType) {
        verifyRun("db list-embedding-providers ${DatabaseName}", outputType, opts);
    }
}
