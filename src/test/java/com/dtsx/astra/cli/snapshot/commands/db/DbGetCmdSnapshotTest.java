package com.dtsx.astra.cli.snapshot.commands.db;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.testlib.Fixtures;
import com.dtsx.astra.cli.testlib.doubles.gateways.DbGatewayStub;
import com.dtsx.astra.sdk.db.domain.Database;

import java.util.Optional;

import static com.dtsx.astra.cli.testlib.AssertUtils.assertEquals;

public class DbGetCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier foundDbOpts = (o) -> o.gateway(new DbGatewayStub() {
        @Override
        public Optional<Database> tryFindOne(DbRef ref) {
            assertEquals(ref, Fixtures.DatabaseName);
            return Optional.of(Fixtures.Database);
        }
    });

    private final SnapshotTestOptionsModifier notFoundDbOpts = (o) -> o.gateway(new DbGatewayStub() {
        @Override
        public Optional<Database> tryFindOne(DbRef ref) {
            assertEquals(ref, "<whatever>");
            return Optional.empty();
        }
    });

    @TestForAllOutputs
    public void db_full_info(OutputType outputType) {
        verifyRun("db get ${DatabaseName}", outputType, foundDbOpts);
    }

    @TestForAllOutputs
    public void db_partial_info(OutputType outputType) {
        verifyRun("db get ${DatabaseName} --key keyspaces", outputType, foundDbOpts);
    }

    @TestForAllOutputs
    public void db_not_found(OutputType outputType) {
        verifyRun("db get <whatever>", outputType, notFoundDbOpts);
    }
}
