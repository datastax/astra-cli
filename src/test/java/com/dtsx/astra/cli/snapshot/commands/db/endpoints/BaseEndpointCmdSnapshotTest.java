package com.dtsx.astra.cli.snapshot.commands.db.endpoints;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public abstract class BaseEndpointCmdSnapshotTest extends BaseCmdSnapshotTest {
    protected final SnapshotTestOptionsModifier opts = o -> o
        .gateway(DbGateway.class, (mock) -> {
            when(mock.findOne(any())).thenReturn(Databases.One);
        })
        .verify((mocks) -> {
            verify(mocks.dbGateway()).findOne(Databases.IdRef);
        });

    public abstract void endpoint_found(OutputType outputType);

    public abstract void endpoint_found_for_region(OutputType outputType);
}
