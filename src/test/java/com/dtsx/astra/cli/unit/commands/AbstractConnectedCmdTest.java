package com.dtsx.astra.cli.unit.commands;

import com.dtsx.astra.cli.commands.AbstractConnectedCmd;
import com.dtsx.astra.cli.commands.AbstractConnectedCmd.ProfileSource;
import com.dtsx.astra.cli.commands.ConnectionOptions;
import com.dtsx.astra.cli.commands.ConnectionOptions.ConfigSpec;
import com.dtsx.astra.cli.commands.ConnectionOptions.CredsSpec;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.properties.CliPropertiesImpl;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import com.dtsx.astra.cli.testlib.extensions.context.UseTestCtx;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.SneakyThrows;
import lombok.val;
import net.jqwik.api.ForAll;
import net.jqwik.api.Group;
import net.jqwik.api.Property;
import net.jqwik.api.PropertyDefaults;
import net.jqwik.api.constraints.UseType;
import org.apache.commons.lang3.tuple.Pair;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

@Group
@PropertyDefaults(tries = 25)
public class AbstractConnectedCmdTest {
    @UseTestCtx(fs = "jimfs")
    private TestCliContext ctx;

    @Group
    public class profileAndSource {
        @Group
        public class Forced {
            @Property
            public void always_uses_forced_test_profile(@ForAll Profile profile, @ForAll @UseType ConnectionOptions connOpts) {
                ctx.modify(c -> c.withForceProfileForTesting(Optional.of(profile)));

                val cmd = mkCmdWithOpts(ctx, connOpts);

                assertThat(cmd.profileAndSource()).isEqualTo(Pair.of(profile, new ProfileSource.Forced(profile)));
            }
        }

        @Group
        public class FromArgs {
            @Property
            public void reconstructs_profile_from_args_given_env(@ForAll AstraToken token, @ForAll AstraEnvironment env) {
                test_reconstructs_profile_from_args(token, Optional.of(env), env);
            }

            @Property
            public void reconstructs_profile_from_args_defaulting_env(@ForAll AstraToken token) {
                test_reconstructs_profile_from_args(token, Optional.empty(), AstraEnvironment.PROD);
            }

            private void test_reconstructs_profile_from_args(AstraToken token, Optional<AstraEnvironment> givenEnv, AstraEnvironment expectedEnv) {
                val opts = mkOpts(o -> o.$creds = new CredsSpec(token, givenEnv));
                val cmd = mkCmdWithOpts(opts);

                val expectedProfile = new Profile(Optional.empty(), token, expectedEnv, Optional.empty());
                val expectedSource = new ProfileSource.FromArgs(token, expectedEnv);

                assertThat(cmd.profileAndSource()).isEqualTo(Pair.of(expectedProfile, expectedSource));
            }
        }

        @Group
        public class CustomFile {
            @Property
            public void loads_given_profile_from_custom_file(@ForAll Profile profile) {
                test_loading_profile_from_custom_file(profile, "custom/location/astrarc", profile.name());
            }

            @Property
            public void loads_default_profile_from_custom_file(@ForAll Profile baseProfile) {
                val defaultProfile = new Profile(Optional.of(ProfileName.DEFAULT), baseProfile.token(), baseProfile.env(), baseProfile.sourceForDefault());
                test_loading_profile_from_custom_file(defaultProfile, "i/like/cars", Optional.empty());
            }

            @Property
            public void loads_env_var_controlled_profile_from_custom_file(@ForAll Profile profile) {
                ctx.modify(c -> c.withProperties(new CliPropertiesImpl() {
                    @Override
                    public String useProfile() {
                        return profile.name().orElseThrow().unwrap();
                    }
                }));
                test_loading_profile_from_custom_file(profile, "not/a.json", Optional.empty());
            }

            private void test_loading_profile_from_custom_file(Profile profile, String rcFilePathStr, Optional<ProfileName> argsProfile) {
                assumeThat(profile.isReconstructedFromCreds()).isFalse();

                val rcFilePath = initRcFile(rcFilePathStr, profile);

                val opts = mkOpts(o -> o.$config = new ConfigSpec(Optional.of(rcFilePath), argsProfile));
                val cmd = mkCmdWithOpts(opts);

                val expectedSource = new ProfileSource.CustomFile(rcFilePath, profile.nameOrDefault());

                assertThat(cmd.profileAndSource()).isEqualTo(Pair.of(profile, expectedSource));
            }
        }

        @Group
        public class DefaultFile {
            // TODO
        }
    }

    @SneakyThrows
    private Path initRcFile(String pathStr, Profile profile) {
        return initRcFile(pathStr, """
          [%s]
          ASTRA_DB_APPLICATION_TOKEN=%s
          %s
          %s
        """.formatted(
            profile.name().orElseThrow(),
            profile.token().unsafeUnwrap(),
            profile.env() == AstraEnvironment.PROD ? "" : "ASTRA_ENV=" + profile.env().name(),
            profile.sourceForDefault().map(s -> "PROFILE_SOURCE=" + s.unwrap()).orElse("")
        ));
    }

    @SneakyThrows
    private Path initRcFile(String pathStr, String content) {
        val path = ctx.get().path(pathStr);
        Files.createDirectories(path.getParent());
        Files.writeString(path, trimIndent(content));
        return path;
    }

    private ConnectionOptions mkOpts(Consumer<ConnectionOptions> useOpts) {
        val opts = new ConnectionOptions();
        useOpts.accept(opts);
        return opts;
    }

    @SneakyThrows
    private AbstractConnectedCmd<?> mkCmdWithOpts(ConnectionOptions opts) {
        return mkCmdWithOpts(ctx, opts);
    }

    @SneakyThrows
    private AbstractConnectedCmd<?> mkCmdWithOpts(TestCliContext ctx, ConnectionOptions opts) {
        val instance = new AbstractConnectedCmd<>() {
            protected Operation<Object> mkOperation() { return null; }
        };
        instance.initCtx(ctx.ref());

        val field = AbstractConnectedCmd.class.getDeclaredField("$connOpts");
        field.setAccessible(true);
        field.set(instance, opts);

        return instance;
    }
}
