package com.dtsx.astra.cli.snapshot.commands.db.keyspace;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway.FoundKeyspaces;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForHumanOutput;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KeyspaceListCmdSnapshotTest extends BaseCmdSnapshotTest {
    private SnapshotTestOptionsModifier mkOpts(FoundKeyspaces ret) {
        return (o) -> o
            .gateway(KeyspaceGateway.class, (mock) -> {
                when(mock.findAll(any())).thenReturn(ret);
            })
            .verify((mocks) -> {
                verify(mocks.keyspaceGateway()).findAll(Databases.IdRef);
            });
    }

    @TestForAllOutputs
    public void keyspaces_found(OutputType outputType) {
        verifyRun("db list-keyspaces ${DatabaseId}", outputType, mkOpts(new FoundKeyspaces("default", List.of("car", "truck", "default"))));
    }

    @TestForHumanOutput
    public void no_keyspaces_found(OutputType outputType) {
        verifyRun("db list-keyspaces ${DatabaseId}", outputType, mkOpts(new FoundKeyspaces(null, List.of())));
    }
}
