package com.dtsx.astra.cli.unit.misc;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.testlib.Fixtures;
import com.dtsx.astra.cli.testlib.Fixtures.Databases;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import lombok.Cleanup;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.List;
import java.util.Set;

import static com.dtsx.astra.cli.testlib.extensions.context.TestCliContextOptions.emptyTestCliContextOptionsBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@Isolated
@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(SystemStubsExtension.class)
public class AstraDefaultArgsTest {
    private final Set<String> expectedDbListOutput = Set.of(
        "\"OK\"",
        "two_regions_db",
        "\"orgId\" : \"8c731576-345b-4bd9-bde1-798dcca0e5f8\""
    );

    @Test
    public void can_set_basic_default_args(EnvironmentVariables env) {
        ensure_no_output_difference(
            List.of("db", "list", "--verbose", "-o", "json"),
            env, "--verbose -o json",
            List.of("db", "list"),
            expectedDbListOutput
        );
    }

    @Test
    public void can_technically_control_whole_command(EnvironmentVariables env) {
        ensure_no_output_difference(
            List.of("db", "list", "--verbose", "-o", "json"),
            env, "db list --verbose -o json",
            List.of(),
            expectedDbListOutput
        );
    }

    @Test
    public void is_overridden_by_explicit_params(EnvironmentVariables env) {
        ensure_no_output_difference(
            List.of("db", "list", "--color", "-o", "json"),
            env, "--color -o csv",
            List.of("db", "list", "-o", "json"),
            expectedDbListOutput
        );
    }

    @Test
    public void errors_confusingly_if_invalid_args_syntax(EnvironmentVariables env) {
        ensure_no_output_difference(
            List.of("-o", "db", "list", "-o", "json"),
            env, "-o",
            List.of("db", "list", "-o", "json"),
            Set.of("Expected parameter for option '--output' but found 'db'")
        );
    }

    @Test
    public void errors_confusingly_if_unexpected_arg_used(EnvironmentVariables env) {
        ensure_no_output_difference(
            List.of("--profile", "dev", "db", "list"),
            env, "--profile dev",
            List.of("db", "list"),
            Set.of("Unknown options: '--profile', 'dev'")
        );
    }

    private void ensure_no_output_difference(List<String> allArgs, EnvironmentVariables env, String envArgs, List<String> partialArgs, Set<String> contains) {
        @Cleanup val ctx1 = mkCtx();
        @Cleanup val ctx2 = mkCtx();

        AstraCli.run(ctx1.ref(), allArgs.toArray(new String[0]));
        assertThat(ctx1.rawOutput().toString()).contains(contains);

        env.set("ASTRA_DEFAULT_ARGS", envArgs);
        AstraCli.run(ctx2.ref(), partialArgs.toArray(new String[0]));
        assertThat(ctx2.rawOutput().toString()).contains(contains);

        assertThat(ctx1.rawOutput()).isNotEmpty().isEqualTo(ctx2.rawOutput());
    }

    private TestCliContext mkCtx() {
        return new TestCliContext(
            emptyTestCliContextOptionsBuilder()
                .gateway(DbGateway.class, (mock) -> when(mock.findAll()).thenReturn(Databases.Many.stream()))
                .forceProfile(Fixtures.Profile)
        );
    }
}
