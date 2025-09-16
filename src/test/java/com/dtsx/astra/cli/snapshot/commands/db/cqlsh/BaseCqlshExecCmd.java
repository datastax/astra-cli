package com.dtsx.astra.cli.snapshot.commands.db.cqlsh;

import com.dtsx.astra.cli.core.exceptions.internal.db.DbNotFoundException;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.SnapshotTestOptions.SnapshotTestOptionsModifier;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;
import com.dtsx.astra.cli.testlib.extensions.binaries.MockInstall;

import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class BaseCqlshExecCmd extends BaseCmdSnapshotTest {
    @MockInstall("cqlsh")
    protected Consumer<DownloadsGateway> mockCqlshInstall;

    @MockInstall("scb")
    protected Consumer<DownloadsGateway> mockScbInstall;

    private final SnapshotTestOptionsModifier downloadGatewayMock = (o) -> o
        .useRealFs()
        .gateway(DownloadsGateway.class, (mock) -> {
            mockCqlshInstall.accept(mock);
            mockScbInstall.accept(mock);
        });

    protected final SnapshotTestOptionsModifier baseDbFoundOpts = (o) -> o
        .use(downloadGatewayMock)
        .gateway(DbGateway.class, (mock) -> {
            when(mock.findOne(any())).thenReturn(Databases.One);
        });

    protected final SnapshotTestOptionsModifier baseDbNotFoundOpts = (o) -> o
        .use(downloadGatewayMock)
        .gateway(DbGateway.class, (mock) -> {
            when(mock.findOne(any())).thenThrow(new DbNotFoundException(Databases.NameRef));
        });
}
