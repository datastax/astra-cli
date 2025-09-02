package com.dtsx.astra.cli.snapshot.commands.db;

import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.snapshot.annotations.TestForAllOutputs;
import com.dtsx.astra.cli.snapshot.annotations.TestForHumanOutput;
import com.dtsx.astra.cli.testlib.Fixtures;
import com.dtsx.astra.cli.testlib.doubles.gateways.DbGatewayStub;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;

import java.time.Duration;
import java.util.Optional;

import static com.dtsx.astra.cli.testlib.AssertUtils.assertEquals;

public class DbDeleteCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier deleteDbOpts = (b) -> b.gateway(new DbGatewayStub() {
        @Override
        public Optional<Database> tryFindOne(DbRef ref) {
            assertEquals(ref, Fixtures.DatabaseName);
            return Optional.of(Fixtures.Database);
        }

        @Override
        public DeletionStatus<DbRef> delete(DbRef ref) {
            assertEquals(ref, Fixtures.DatabaseName);
            return DeletionStatus.deleted(ref);
        }

        @Override
        public Duration waitUntilDbStatus(DbRef ref, DatabaseStatusType target, int timeout) {
            return Duration.ofMillis(6789);
        }
    });

    @TestForAllOutputs
    public void db_force_delete(OutputType outputType) {
        verifyRun("db delete ${DatabaseName} --yes", outputType, o -> o.use(deleteDbOpts));
    }

    @TestForAllOutputs
    public void error_when_no_confirmation(OutputType outputType) {
        verifyRun("db delete ${DatabaseName}", outputType, o -> o.use(deleteDbOpts));
    }

    @TestForHumanOutput
    public void db_delete_with_confirmation(OutputType outputType) {
        verifyRun("db delete ${DatabaseName}", outputType, o -> o.use(deleteDbOpts)
            .stdin("yes"));
    }
}
