package com.dtsx.astra.cli.snapshot.commands.scenarios.env_var_control;

import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.snapshot.BaseCmdSnapshotTest;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import static org.mockito.Mockito.when;

@Isolated
@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(SystemStubsExtension.class)
public class AstraDefaultArgsSnapshotTest extends BaseCmdSnapshotTest {
    @Test
    public void can_set_basic_default_args(EnvironmentVariables env) {
        env.set("ASTRA_DEFAULT_ARGS", "--verbose -o json");

        verifyRun("db list", OutputType.HUMAN, (o) ->
            o.gateway(DbGateway.class, (mock) -> when(mock.findAll()).thenReturn(Databases.Many.stream()))
        );
    }
}
