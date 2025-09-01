package com.dtsx.astra.cli.snapshot.commands.db;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.testlib.Fixtures;
import com.dtsx.astra.cli.testlib.doubles.gateways.DbGatewayStub;
import com.dtsx.astra.sdk.db.domain.Database;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;

public class DbGetCmdSnapshotTest extends BaseCmdSnapshotTest {
    private final SnapshotTestOptionsModifier foundDbOpts = (b) -> b.gateway(new DbGatewayStub() {
        @Override
        public Optional<Database> tryFindOne(DbRef ref) {
            return Optional.of(Fixtures.Database);
        }
    });

    @Nested
    public class human {
        @Test
        public void gets_full_information_about_a_database() {
            val output = run("db get hi", foundDbOpts);

            System.out.println(output.toSnapshot());
        }
    }
}
