package com.dtsx.astra.cli.unit.core.config;


import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.datatypes.Either;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.config.AstraConfigFileException;
import com.dtsx.astra.cli.testlib.Fixtures;
import com.dtsx.astra.cli.testlib.extensions.context.TestCliContext;
import com.dtsx.astra.cli.testlib.extensions.context.UseTestCtx;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.SneakyThrows;
import lombok.val;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.util.List;

import static com.dtsx.astra.cli.core.output.ExitCode.FILE_ISSUE;
import static com.dtsx.astra.cli.core.output.ExitCode.PARSE_ISSUE;
import static com.dtsx.astra.cli.utils.StringUtils.trimIndent;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class AstraConfigTest {
    @UseTestCtx(fs = "jimfs")
    private TestCliContext ctx;

    @Nested
    public class errors {
        @Test
        public void default_file_not_found() {
            assertThatThrownBy(() -> AstraConfig.readAstraConfigFile(ctx.get(), null, false))
                .asInstanceOf(InstanceOfAssertFactories.throwable(AstraCliException.class))
                .hasMessageContaining("Error: The default configuration file (%s) does not exist.", AstraConfig.resolveDefaultAstraConfigFile(ctx.get()))
                .satisfies(ex -> assertThat(ex.getCode()).isEqualTo(FILE_ISSUE));
        }

        @Test
        public void given_file_not_found() {
            val path = ctx.get().path("does/not/exist");

            assertThatThrownBy(() -> AstraConfig.readAstraConfigFile(ctx.get(), path, false))
                .asInstanceOf(InstanceOfAssertFactories.throwable(AstraCliException.class))
                .hasMessageContaining("Error: The given configuration file at %s could not be found.", path)
                .satisfies(ex -> assertThat(ex.getCode()).isEqualTo(FILE_ISSUE));
        }

        @Test
        @SneakyThrows
        public void ini_parse_exceptions_wrapped_and_rethrown() {
            val path = ctx.get().path(".astrarc");

            Files.writeString(path, "!@#12312312");

            assertThatThrownBy(() -> AstraConfig.readAstraConfigFile(ctx.get(), path, false))
                .asInstanceOf(InstanceOfAssertFactories.throwable(AstraCliException.class))
                .hasMessageContaining("An error occurred parsing the configuration file '.astrarc'")
                .hasMessageContaining("Unknown syntax")
                .satisfies(ex -> assertThat(ex.getCode()).isEqualTo(PARSE_ISSUE));
        }
    }

    @Nested
    public class lifecycle {
        @Test
        @SneakyThrows
        public void creating_new_file() {
            val config = AstraConfig.readAstraConfigFile(ctx.get(), null, true);

            assertThat(config.profiles()).isEmpty();
            assertThat(config.backingFile()).isRegularFile();

            // add initial profile
            config.modify((ctx) -> {
                ctx.createProfile(ProfileName.mkUnsafe("my-profile"), Fixtures.Token, AstraEnvironment.PROD);
            });

            assertThat(config.profiles()).hasSize(1);
            assertThat(config.profilesValidated()).hasSize(1);

            assertThat(config.profiles().getFirst())
                .extracting(Either::getRight)
                .satisfies((p) -> {
                    assertThat(p.name()).contains(ProfileName.mkUnsafe("my-profile"));
                    assertThat(p.token().unsafeUnwrap()).isEqualTo(Fixtures.Token.unsafeUnwrap());
                    assertThat(p.env()).isEqualTo(AstraEnvironment.PROD);
                });

            // set it as default
            config.modify((ctx) -> {
                ctx.copyProfile(config.lookupProfile(ProfileName.mkUnsafe("my-profile")).orElseThrow(), ProfileName.DEFAULT);
            });

            assertThat(config.profilesValidated()).hasSize(2);

            assertThat(config.profilesValidated().getLast())
                .extracting(Profile::nameOrDefault)
                .extracting(ProfileName::unwrap)
                .isEqualTo("default");

            // add profiles in different envs
            config.modify((ctx) -> {
                ctx.createProfile(ProfileName.mkUnsafe("dev"), Fixtures.Token, AstraEnvironment.DEV);
                ctx.createProfile(ProfileName.mkUnsafe("test"), Fixtures.Token, AstraEnvironment.TEST);
            });

            assertThat(config.profilesValidated()).hasSize(4);

            config.modify((ctx) -> {
                ctx.deleteProfile(ProfileName.mkUnsafe("dev"));
            });

            assertThat(config.backingFile()).hasContent(trimIndent("""
              [my-profile]
              ASTRA_DB_APPLICATION_TOKEN=${token}
            
              [default]
              ASTRA_DB_APPLICATION_TOKEN=${token}
            
              [test]
              ASTRA_DB_APPLICATION_TOKEN=${token}
              ASTRA_ENV=TEST
            """.replace("${token}", Fixtures.Token.unsafeUnwrap())));
        }

        @Test
        @SneakyThrows
        public void from_existing_file() {
            val path = ctx.get().path("yay config yay");

            Files.writeString(path, trimIndent("""
              # invalid profile (missing token key)
              [default]
            
              # invalid profile (invalid token)
              [invalid/bad-token]
              ASTRA_DB_APPLICATION_TOKEN=not-a-valid-token
            
              # invalid profile (invalid token)
              [invalid/bad-env]
              ASTRA_DB_APPLICATION_TOKEN=${token}
              # not a valid env (line comment)
              ASTRA_ENV=not-a-valid-env
            
              # invalid profile (invalid name)
              [<invalid/bad-name>]
              ASTRA_DB_APPLICATION_TOKEN=${token}
            
              # valid profile w/ default env
              [valid/default]
              ASTRA_DB_APPLICATION_TOKEN=${token}
            
              # valid profile w/ custom env
              [valid/dev]
              ASTRA_DB_APPLICATION_TOKEN=${token}
              ASTRA_ENV=dev
           
              # duplicate profile
              [semi-invalid/duplicate]
              # duplicate (line comment)
              ASTRA_DB_APPLICATION_TOKEN=${token}
            
              # duplicate profile
              [semi-invalid/duplicate]
              ASTRA_DB_APPLICATION_TOKEN=${token}
            """.replace("${token}", Fixtures.Token.unsafeUnwrap())));

            val config = AstraConfig.readAstraConfigFile(ctx.get(), path, false);

            assertThat(config.profiles()).hasSize(8);

            // only errors on one profile at a time, sequentially
            assertThatThrownBy(config::profilesValidated)
                .asInstanceOf(InstanceOfAssertFactories.throwable(AstraConfigFileException.class))
                .hasMessageContaining("default")
                .hasMessageContaining("Missing the required key 'ASTRA_DB_APPLICATION_TOKEN'")
                .satisfies(ex -> assertThat(ex.getCode()).isEqualTo(PARSE_ISSUE));

            // invalid profile (missing token key)
            assertThat(config.profiles().getFirst())
                .extracting(Either::getLeft)
                .satisfies((ip) -> {
                    assertThat(ip.section().name()).isEqualTo("default");
                    assertThat(ip.issue()).contains("Missing the required key 'ASTRA_DB_APPLICATION_TOKEN'");
                });

            // invalid profile (invalid token)
            assertThat(config.profiles().get(1))
                .extracting(Either::getLeft)
                .satisfies((ip) -> {
                    assertThat(ip.section().name()).isEqualTo("invalid/bad-token");
                    assertThat(ip.issue()).contains("Error parsing 'ASTRA_DB_APPLICATION_TOKEN': Astra token should start with 'AstraCS:'");
                });

            // invalid profile (invalid env)
            assertThat(config.profiles().get(2))
                .extracting(Either::getLeft)
                .satisfies((ip) -> {
                    assertThat(ip.section().name()).isEqualTo("invalid/bad-env");
                    assertThat(ip.issue()).contains("Error parsing 'ASTRA_ENV': Got 'not-a-valid-env', expected one of (prod|dev|test)");
                });

            // invalid profile (invalid name)
            assertThat(config.profiles().get(3))
                .extracting(Either::getLeft)
                .satisfies((ip) -> {
                    assertThat(ip.section().name()).isEqualTo("<invalid/bad-name>");
                    assertThat(ip.issue()).contains("Error parsing profile name @'!<invalid/bad-name>!@: Profile name should not be enclosed in angle brackets... did you forget to replace a placeholder?");
                });

            // valid profile w/ default env
            assertThat(config.profiles().get(4))
                .extracting(Either::getRight)
                .satisfies((p) -> {
                    assertThat(p.name()).contains(ProfileName.mkUnsafe("valid/default"));
                    assertThat(p.token().unsafeUnwrap()).isEqualTo(Fixtures.Token.unsafeUnwrap());
                    assertThat(p.env()).isEqualTo(AstraEnvironment.PROD);
                });

            // valid profile w/ custom env
            assertThat(config.profiles().get(5))
                .extracting(Either::getRight)
                .satisfies((p) -> {
                    assertThat(p.name()).contains(ProfileName.mkUnsafe("valid/dev"));
                    assertThat(p.token().unsafeUnwrap()).isEqualTo(Fixtures.Token.unsafeUnwrap());
                    assertThat(p.env()).isEqualTo(AstraEnvironment.DEV);
                });

            // duplicate profiles
            val duplicateProfile = config.profiles().get(6);

            for (val index : List.of(6, 7)) {
                assertThat(config.profiles().get(index))
                    .extracting(Either::getRight)
                    .satisfies((p) -> {
                        assertThat(p.name()).contains(ProfileName.mkUnsafe("semi-invalid/duplicate"));
                        assertThat(p.token().unsafeUnwrap()).isEqualTo(Fixtures.Token.unsafeUnwrap());
                        assertThat(p.env()).isEqualTo(AstraEnvironment.PROD);
                    });
            }

            // lookup tests
            assertThat(config.lookupProfile(ProfileName.mkUnsafe("non-existent"))).isEmpty();

            assertThatThrownBy(() -> config.lookupProfile(ProfileName.mkUnsafe("semi-invalid/duplicate")))
                .asInstanceOf(InstanceOfAssertFactories.throwable(AstraConfigFileException.class))
                .hasMessageContaining("Multiple profiles found for name @'!semi-invalid/duplicate!@");

            assertThat(config.lookupProfile(ProfileName.mkUnsafe("valid/dev")))
                .isPresent()
                .get()
                .satisfies((p) -> {
                    assertThat(p.name()).contains(ProfileName.mkUnsafe("valid/dev"));
                    assertThat(p.token().unsafeUnwrap()).isEqualTo(Fixtures.Token.unsafeUnwrap());
                    assertThat(p.env()).isEqualTo(AstraEnvironment.DEV);
                });

            // existence tests
            assertThat(config.profileExists(ProfileName.mkUnsafe("non-existent"))).isFalse();

            assertThat(config.profileExists(ProfileName.mkUnsafe("valid/default"))).isTrue();
            assertThat(config.profileExists(ProfileName.mkUnsafe("semi-invalid/duplicate"))).isTrue();
            assertThat(config.profileExists(ProfileName.mkUnsafe("valid/dev"))).isTrue();

            // getting a raw section
            assertThat(config.lookupSection("non-existent")).isEmpty();

            assertThat(config.lookupSection("invalid/bad-env"))
                .isPresent()
                .get()
                .satisfies((ip) -> {
                    assertThat(ip.name()).isEqualTo("invalid/bad-env");
                    assertThat(ip.lookupKey("ASTRA_DB_APPLICATION_TOKEN")).contains(Fixtures.Token.unsafeUnwrap());
                    assertThat(ip.lookupKey("ASTRA_ENV")).contains("not-a-valid-env");
                });

            // attempt to fix invalid profile
            config.modify((ctx) -> {
                ctx.deleteProfile(ProfileName.mkUnsafe("invalid/bad-env"));
                ctx.createProfile(ProfileName.mkUnsafe("invalid/bad-env"), Fixtures.Token, AstraEnvironment.TEST);
            });

            assertThat(config.profiles().getLast())
                .extracting(Either::getRight)
                .satisfies((p) -> {
                    assertThat(p.name().orElseThrow().unwrap()).isEqualTo("invalid/bad-env");
                    assertThat(p.token().unsafeUnwrap()).isEqualTo(Fixtures.Token.unsafeUnwrap());
                    assertThat(p.env()).isEqualTo(AstraEnvironment.TEST);
                });

            // attempt to fix duplicate profile
            config.modify((ctx) -> {
                ctx.copyProfile(duplicateProfile.getRight(), duplicateProfile.getRight().nameOrDefault());
            });

            assertThat(config.lookupProfile(ProfileName.mkUnsafe("semi-invalid/duplicate")))
                .isPresent()
                .get()
                .satisfies((p) -> {
                    assertThat(p.name()).contains(ProfileName.mkUnsafe("semi-invalid/duplicate"));
                    assertThat(p.token().unsafeUnwrap()).isEqualTo(Fixtures.Token.unsafeUnwrap());
                    assertThat(p.env()).isEqualTo(AstraEnvironment.PROD);
                });

            assertThat(config.backingFile()).hasContent(trimIndent("""
              # invalid profile (missing token key)
              [default]
            
              # invalid profile (invalid token)
              [invalid/bad-token]
              ASTRA_DB_APPLICATION_TOKEN=not-a-valid-token
            
              # invalid profile (invalid token)
              # invalid profile (invalid name)
              [<invalid/bad-name>]
              ASTRA_DB_APPLICATION_TOKEN=${token}
            
              # valid profile w/ default env
              [valid/default]
              ASTRA_DB_APPLICATION_TOKEN=${token}
            
              # valid profile w/ custom env
              [valid/dev]
              ASTRA_DB_APPLICATION_TOKEN=${token}
              ASTRA_ENV=dev
            
              # duplicate profile
              # duplicate profile
              [invalid/bad-env]
              ASTRA_DB_APPLICATION_TOKEN=${token}
              ASTRA_ENV=TEST
            
              [semi-invalid/duplicate]
              # duplicate (line comment)
              ASTRA_DB_APPLICATION_TOKEN=${token}
            """.replace("${token}", Fixtures.Token.unsafeUnwrap())));
        }
    }
}
