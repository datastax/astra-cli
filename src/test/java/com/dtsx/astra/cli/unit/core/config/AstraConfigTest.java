package com.dtsx.astra.cli.unit.core.config;

import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.config.ProfileName;
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
                .satisfies(ex -> assertThat(ex.code()).isEqualTo(FILE_ISSUE));
        }

        @Test
        public void given_file_not_found() {
            val path = ctx.get().path("does/not/exist");

            assertThatThrownBy(() -> AstraConfig.readAstraConfigFile(ctx.get(), path, false))
                .asInstanceOf(InstanceOfAssertFactories.throwable(AstraCliException.class))
                .hasMessageContaining("Error: The given configuration file at %s could not be found.", path)
                .satisfies(ex -> assertThat(ex.code()).isEqualTo(FILE_ISSUE));
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
                .satisfies(ex -> assertThat(ex.code()).isEqualTo(PARSE_ISSUE));
        }

        @Test
        @SneakyThrows
        public void invalid_profile_missing_token_fails_fast() {
            val path = ctx.get().path(".astrarc");
            Files.writeString(path, trimIndent("""
              [default]
            """));

            assertThatThrownBy(() -> AstraConfig.readAstraConfigFile(ctx.get(), path, false))
                .asInstanceOf(InstanceOfAssertFactories.throwable(AstraConfigFileException.class))
                .hasMessageContaining("default")
                .hasMessageContaining("Missing required key")
                .hasMessageContaining("ASTRA_DB_APPLICATION_TOKEN")
                .satisfies(ex -> assertThat(ex.code()).isEqualTo(PARSE_ISSUE));
        }

        @Test
        @SneakyThrows
        public void invalid_profile_bad_token_fails_fast() {
            val path = ctx.get().path(".astrarc");
            Files.writeString(path, trimIndent("""
              [my-profile]
              ASTRA_DB_APPLICATION_TOKEN=not-a-valid-token
            """));

            assertThatThrownBy(() -> AstraConfig.readAstraConfigFile(ctx.get(), path, false))
                .asInstanceOf(InstanceOfAssertFactories.throwable(AstraConfigFileException.class))
                .hasMessageContaining("my-profile")
                .hasMessageContaining("ASTRA_DB_APPLICATION_TOKEN")
                .hasMessageContaining("Astra token should start with 'AstraCS:'")
                .satisfies(ex -> assertThat(ex.code()).isEqualTo(PARSE_ISSUE));
        }

        @Test
        @SneakyThrows
        public void invalid_profile_bad_env_fails_fast() {
            val path = ctx.get().path(".astrarc");
            Files.writeString(path, trimIndent("""
              [my-profile]
              ASTRA_DB_APPLICATION_TOKEN=${token}
              ASTRA_ENV=not-a-valid-env
            """.replace("${token}", Fixtures.Token.unsafeUnwrap())));

            assertThatThrownBy(() -> AstraConfig.readAstraConfigFile(ctx.get(), path, false))
                .asInstanceOf(InstanceOfAssertFactories.throwable(AstraConfigFileException.class))
                .hasMessageContaining("my-profile")
                .hasMessageContaining("ASTRA_ENV")
                .hasMessageContaining("Got 'not-a-valid-env', expected one of (prod|dev|test|local)")
                .satisfies(ex -> assertThat(ex.code()).isEqualTo(PARSE_ISSUE));
        }

        @Test
        @SneakyThrows
        public void invalid_profile_bad_name_fails_fast() {
            val path = ctx.get().path(".astrarc");
            Files.writeString(path, trimIndent("""
              [<invalid-name>]
              ASTRA_DB_APPLICATION_TOKEN=${token}
            """.replace("${token}", Fixtures.Token.unsafeUnwrap())));

            assertThatThrownBy(() -> AstraConfig.readAstraConfigFile(ctx.get(), path, false))
                .asInstanceOf(InstanceOfAssertFactories.throwable(AstraConfigFileException.class))
                .hasMessageContaining("<invalid-name>")
                .hasMessageContaining("Error parsing profile name")
                .hasMessageContaining("Profile name should not be enclosed in angle brackets... did you forget to replace a placeholder?")
                .satisfies(ex -> assertThat(ex.code()).isEqualTo(PARSE_ISSUE));
        }

        @Test
        @SneakyThrows
        public void invalid_profile_bad_source_for_default_fails_fast() {
            val path = ctx.get().path(".astrarc");
            Files.writeString(path, trimIndent("""
              [default]
              ASTRA_DB_APPLICATION_TOKEN=${token}
              DEFAULT_PROFILE_SOURCE=<invalid-source-name>
            """.replace("${token}", Fixtures.Token.unsafeUnwrap())));

            assertThatThrownBy(() -> AstraConfig.readAstraConfigFile(ctx.get(), path, false))
                .asInstanceOf(InstanceOfAssertFactories.throwable(AstraConfigFileException.class))
                .hasMessageContaining("default")
                .hasMessageContaining("DEFAULT_PROFILE_SOURCE")
                .hasMessageContaining("Profile name should not be enclosed in angle brackets")
                .satisfies(ex -> assertThat(ex.code()).isEqualTo(PARSE_ISSUE));
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

            assertThat(config.profiles().getFirst())
                .satisfies((p) -> {
                    assertThat(p.name()).contains(ProfileName.mkUnsafe("my-profile"));
                    assertThat(p.token().unsafeUnwrap()).isEqualTo(Fixtures.Token.unsafeUnwrap());
                    assertThat(p.env()).isEqualTo(AstraEnvironment.PROD);
                });

            // set it as default
            config.modify((ctx) -> {
                ctx.copyProfile(ProfileName.mkUnsafe("my-profile"), ProfileName.DEFAULT);
            });

            assertThat(config.profiles()).hasSize(2);

            assertThat(config.profiles().getLast())
                .extracting(Profile::nameOrDefault)
                .extracting(ProfileName::unwrap)
                .isEqualTo("default");

            // add profiles in different envs
            config.modify((ctx) -> {
                ctx.createProfile(ProfileName.mkUnsafe("dev"), Fixtures.Token, AstraEnvironment.DEV);
                ctx.createProfile(ProfileName.mkUnsafe("test"), Fixtures.Token, AstraEnvironment.TEST);
            });

            assertThat(config.profiles()).hasSize(4);

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
        public void from_existing_valid_file() {
            val path = ctx.get().path("yay config yay");

            Files.writeString(path, trimIndent("""
              # default profile with source link
              [default]
              ASTRA_DB_APPLICATION_TOKEN=${token}
              DEFAULT_PROFILE_SOURCE=valid/dev

              # valid profile w/ default env
              [valid/default]
              ASTRA_DB_APPLICATION_TOKEN=${token}

              # valid profile w/ custom env
              [valid/dev]
              ASTRA_DB_APPLICATION_TOKEN=${token}
              ASTRA_ENV=dev

              # duplicate profile
              [semi-invalid/duplicate]
              ASTRA_DB_APPLICATION_TOKEN=${token}

              # duplicate profile
              [semi-invalid/duplicate]
              ASTRA_DB_APPLICATION_TOKEN=${token}
            """.replace("${token}", Fixtures.Token.unsafeUnwrap())));

            val config = AstraConfig.readAstraConfigFile(ctx.get(), path, false);

            assertThat(config.profiles()).hasSize(5);

            // default profile with sourceForDefault
            assertThat(config.profiles().getFirst())
                .satisfies((p) -> {
                    assertThat(p.name()).contains(ProfileName.DEFAULT);
                    assertThat(p.token().unsafeUnwrap()).isEqualTo(Fixtures.Token.unsafeUnwrap());
                    assertThat(p.env()).isEqualTo(AstraEnvironment.PROD);
                    assertThat(p.sourceForDefault()).contains(ProfileName.mkUnsafe("valid/dev"));
                });

            // valid profile w/ custom env
            assertThat(config.lookupProfile(ProfileName.mkUnsafe("valid/dev")))
                .isPresent()
                .get()
                .satisfies((p) -> {
                    assertThat(p.name()).contains(ProfileName.mkUnsafe("valid/dev"));
                    assertThat(p.token().unsafeUnwrap()).isEqualTo(Fixtures.Token.unsafeUnwrap());
                    assertThat(p.env()).isEqualTo(AstraEnvironment.DEV);
                });

            // lookup non-existent
            assertThat(config.lookupProfile(ProfileName.mkUnsafe("non-existent"))).isEmpty();

            // lookup duplicate throws exception
            assertThatThrownBy(() -> config.lookupProfile(ProfileName.mkUnsafe("semi-invalid/duplicate")))
                .asInstanceOf(InstanceOfAssertFactories.throwable(AstraConfigFileException.class))
                .hasMessageContaining("Multiple profiles found for name @'!semi-invalid/duplicate!@");

            // existence tests
            assertThat(config.profileExists(ProfileName.mkUnsafe("non-existent"))).isFalse();
            assertThat(config.profileExists(ProfileName.mkUnsafe("valid/default"))).isTrue();
            assertThat(config.profileExists(ProfileName.mkUnsafe("valid/dev"))).isTrue();
            assertThat(config.profileExists(ProfileName.DEFAULT)).isTrue();

            // lookupSection
            assertThat(config.lookupSection("non-existent")).isEmpty();
            assertThat(config.lookupSection("valid/dev"))
                .isPresent()
                .get()
                .satisfies((s) -> {
                    assertThat(s.name()).isEqualTo("valid/dev");
                    assertThat(s.lookupKey("ASTRA_DB_APPLICATION_TOKEN")).contains(Fixtures.Token.unsafeUnwrap());
                    assertThat(s.lookupKey("ASTRA_ENV")).contains("dev");
                });

            // modifying - delete and create
            config.modify((ctx) -> {
                ctx.deleteProfile(ProfileName.mkUnsafe("valid/default"));
                ctx.createProfile(ProfileName.mkUnsafe("new-profile"), Fixtures.Token, AstraEnvironment.TEST);
            });

            assertThat(config.profileExists(ProfileName.mkUnsafe("valid/default"))).isFalse();
            assertThat(config.profileExists(ProfileName.mkUnsafe("new-profile"))).isTrue();
        }

        @Test
        @SneakyThrows
        public void renaming_profile_updates_default_source() {
            val path = ctx.get().path("rename_config");

            Files.writeString(path, trimIndent("""
              [default]
              ASTRA_DB_APPLICATION_TOKEN=${token}
              DEFAULT_PROFILE_SOURCE=old-name

              [old-name]
              ASTRA_DB_APPLICATION_TOKEN=${token}
            """.replace("${token}", Fixtures.Token.unsafeUnwrap())));

            val config = AstraConfig.readAstraConfigFile(ctx.get(), path, false);

            assertThat(config.lookupProfile(ProfileName.DEFAULT).orElseThrow().sourceForDefault())
                .contains(ProfileName.mkUnsafe("old-name"));

            config.modify((ctx) -> {
                ctx.renameProfile(ProfileName.mkUnsafe("old-name"), ProfileName.mkUnsafe("new-name"));
            });

            assertThat(config.profileExists(ProfileName.mkUnsafe("old-name"))).isFalse();
            assertThat(config.profileExists(ProfileName.mkUnsafe("new-name"))).isTrue();

            assertThat(config.lookupProfile(ProfileName.DEFAULT).orElseThrow().sourceForDefault())
                .contains(ProfileName.mkUnsafe("new-name"));

            assertThat(config.lookupSection("default").orElseThrow().lookupKey(AstraConfig.SOURCE_KEY))
                .contains("new-name");
        }
    }
}
