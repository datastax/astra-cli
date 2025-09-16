package com.dtsx.astra.cli.integration.core;

import com.dtsx.astra.cli.core.CliProperties;
import lombok.val;
import net.jqwik.api.Arbitraries;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;
import uk.org.webcompere.systemstubs.properties.SystemProperties;

import java.io.File;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

@Isolated
@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(SystemStubsExtension.class)
public class CliPropertiesIntegrationTest {
    static {
        CliProperties props = null; // force class loading since it uses a static initializer
    }

    @Nested
    public class external_software {
        @Test
        public void cqlsh(SystemProperties systemProps) {
            systemProps.set(
                "cqlsh.url", "<cqlsh_url>",
                "cqlsh.version", "<cqlsh_version>"
            );

            val cqlsh = CliProperties.cqlsh();

            assertThat(cqlsh.url()).isEqualTo("<cqlsh_url>");
            assertThat(cqlsh.version()).isEqualTo("<cqlsh_version>");
        }

        @Test
        public void dsbulk(SystemProperties systemProps) {
            systemProps.set(
                "dsbulk.url", "<dsbulk_url>",
                "dsbulk.version", "<dsbulk_version>"
            );

            val dsbulk = CliProperties.dsbulk();

            assertThat(dsbulk.url()).isEqualTo("<dsbulk_url>");
            assertThat(dsbulk.version()).isEqualTo("<dsbulk_version>");
        }

        @Test
        public void pulsar(SystemProperties systemProps) {
            systemProps.set(
                "pulsar-shell.url", "<pulsar_url>",
                "pulsar-shell.version", "<pulsar_version>"
            );

            val pulsar = CliProperties.pulsar();

            assertThat(pulsar.url()).isEqualTo("<pulsar_url>");
            assertThat(pulsar.version()).isEqualTo("<pulsar_version>");
        }
    }

    @Nested
    public class versioning {
        @Test
        public void static_version(SystemProperties systemProps) {
            systemProps.set(
                "cli.version", "<cli_version>"
            );
            assertThat(CliProperties.version()).isEqualTo("<cli_version>");
        }

        @Test
        public void instance_version(SystemProperties systemProps) {
            systemProps.set(
                "cli.version", "<cli_version>"
            );
            assertThat(new CliProperties().getVersion()).containsExactly("<cli_version>");
        }

        @Test
        public void static_and_instance_version_are_always_equal(SystemProperties systemProps) {
            systemProps.set(
                "cli.version", String.valueOf(Math.random())
            );
            assertThat(new CliProperties().getVersion()).containsExactly(CliProperties.version());
        }
    }

    @Nested
    public class file_names {
        @Test
        public void rc_file_name(SystemProperties systemProps) {
            systemProps.set(
                "cli.rc-file-name", ".astrarc"
            );
            assertThat(CliProperties.rcFileName()).isEqualTo(".astrarc");
        }

        @Test
        public void home_folder_name(SystemProperties systemProps) {
            systemProps.set(
                "cli.home-folder-name", "astra"
            );
            assertThat(CliProperties.homeFolderName(false)).isEqualTo("astra");
            assertThat(CliProperties.homeFolderName(true)).isEqualTo("." + CliProperties.homeFolderName(false));
        }
    }

    @Nested
    public class env_vars {
        @Test
        public void rc_env_var() {
            assertThat(CliProperties.rcEnvVar()).isEqualTo("ASTRARC_PATH");
        }

        @Test
        public void home_env_var() {
            assertThat(CliProperties.homeEnvVar()).isEqualTo("ASTRA_HOME_PATH");
        }
    }

    @Nested
    public class file_paths { // unfortunately can't use jqwik b/c it doesn't work w/. system-stubs
        private final BiFunction<SystemProperties, EnvironmentVariables, Steps> mkRcFileSteps = (sys, env) -> new Steps(
            (path) -> { sys.set("cli.env-vars.rc-file", "CUSTOM_RC_PATH"); env.set(CliProperties.rcEnvVar(), path); },
            (path) -> env.set("XDG_CONFIG_HOME", path),
            (path) -> sys.set("user.home", path),
            (path) -> sys.set("user.home", path)
        );

        private final BiFunction<SystemProperties, EnvironmentVariables, Steps> mkHomeFolderSteps = (sys, env) -> new Steps(
            (path) -> { sys.set("cli.env-vars.home-folder", "CUSTOM_HOME_PATH"); env.set(CliProperties.homeEnvVar(), path); },
            (path) -> env.set("XDG_DATA_HOME", path),
            (path) -> env.set("LOCALAPPDATA", path),
            (path) -> sys.set("user.home", path)
        );

        @Nested
        public class rc_file_path {
            @Test
            public void prioritizes_custom_path(SystemProperties sys, EnvironmentVariables env) {
                val customPath = mkRcFileSteps.apply(sys, env).applyAll();

                // whether it's windows should not matter here
                assertThat(CliProperties.defaultRcFile(true)).isEqualTo(customPath);
                assertThat(System.getProperty("cli.rc-file-path")).isEqualTo(customPath);

                assertThat(CliProperties.defaultRcFile(false)).isEqualTo(customPath);
                assertThat(System.getProperty("cli.rc-file-path")).isEqualTo(customPath);
            }

            @Test
            public void uses_xdg_if_no_custom_path(SystemProperties sys, EnvironmentVariables env) {
                val xdgPath = mkRcFileSteps.apply(sys, env).applyXdgAndDefaults();
                val homePath = mkHomeFolderSteps.apply(sys, env).applyAll();

                val expectedSubpath = File.separator + homePath + File.separator + CliProperties.rcFileName();

                // returned path should not depend on the os; the display path should depend on the os though
                assertThat(CliProperties.defaultRcFile(true)).isEqualTo(xdgPath + expectedSubpath);
                assertThat(System.getProperty("cli.rc-file-path")).isEqualTo("%XDG_CONFIG_HOME%" + expectedSubpath);

                assertThat(CliProperties.defaultRcFile(false)).isEqualTo(xdgPath + expectedSubpath);
                assertThat(System.getProperty("cli.rc-file-path")).isEqualTo("$XDG_CONFIG_HOME" + expectedSubpath);
            }

            @Test
            public void defaults_to_user_home_if_no_path_specified(SystemProperties sys, EnvironmentVariables env) {
                val defaultPath = mkRcFileSteps.apply(sys, env).applyBothDefaults();

                sys.set("cli.rc-file-name", "custom-rc-file");
                val expectedSubpath = File.separator + "custom-rc-file";

                // returned path should not depend on the os; the display path should depend on the os though
                assertThat(CliProperties.defaultRcFile(true)).isEqualTo(defaultPath + expectedSubpath);
                assertThat(System.getProperty("cli.rc-file-path")).isEqualTo("%USERPROFILE%" + expectedSubpath);

                assertThat(CliProperties.defaultRcFile(false)).isEqualTo(defaultPath + expectedSubpath);
                assertThat(System.getProperty("cli.rc-file-path")).isEqualTo("~" + expectedSubpath);
            }
        }

        @Nested
        public class home_folder_path {
            @Test
            public void prioritizes_custom_path(SystemProperties sys, EnvironmentVariables env) {
                val customPath = mkHomeFolderSteps.apply(sys, env).applyAll();

                assertThat(CliProperties.defaultHomeFolder(true)).isEqualTo(customPath);
                assertThat(System.getProperty("cli.home-folder-path")).isEqualTo(customPath);

                assertThat(CliProperties.defaultHomeFolder(false)).isEqualTo(customPath);
                assertThat(System.getProperty("cli.home-folder-path")).isEqualTo(customPath);
            }

            @Test
            public void uses_xdg_if_no_custom_path(SystemProperties sys, EnvironmentVariables env) {
                val xdgPath = mkHomeFolderSteps.apply(sys, env).applyXdgAndDefaults();

                sys.set("cli.home-folder-name", "custom-home-folder");
                val expectedSubpath = File.separator + "custom-home-folder";

                // returned path should not depend on the os; the display path should depend on the os though
                assertThat(CliProperties.defaultHomeFolder(true)).isEqualTo(xdgPath + expectedSubpath);
                assertThat(System.getProperty("cli.home-folder-path")).isEqualTo("%XDG_DATA_HOME%" + expectedSubpath);

                assertThat(CliProperties.defaultHomeFolder(false)).isEqualTo(xdgPath + expectedSubpath);
                assertThat(System.getProperty("cli.home-folder-path")).isEqualTo("$XDG_DATA_HOME" + expectedSubpath);
            }

            @Test
            public void defaults_to_user_home_if_no_path_specified_on_unix(SystemProperties sys, EnvironmentVariables env) {
                val defaultPath = mkHomeFolderSteps.apply(sys, env).applyUnixDefault();

                sys.set("cli.home-folder-name", "custom-home-folder");
                val expectedSubpath = File.separator + ".custom-home-folder";

                assertThat(CliProperties.defaultHomeFolder(false)).isEqualTo(defaultPath + expectedSubpath);
                assertThat(System.getProperty("cli.home-folder-path")).isEqualTo("~" + expectedSubpath);
            }

            @Test
            public void defaults_to_localappdata_if_no_path_specified_on_windows(SystemProperties sys, EnvironmentVariables env) {
                val defaultPath = mkHomeFolderSteps.apply(sys, env).applyWindowsDefault();

                sys.set("cli.home-folder-name", "custom-home-folder");
                val expectedSubpath = File.separator + "custom-home-folder";

                assertThat(CliProperties.defaultHomeFolder(true)).isEqualTo(defaultPath + expectedSubpath);
                assertThat(System.getProperty("cli.home-folder-path")).isEqualTo("%LOCALAPPDATA%" + expectedSubpath);
            }
        }

        private record Steps(Consumer<String> custom, Consumer<String> xdg, Consumer<String> defaultWindows, Consumer<String> defaultUnix) {
            public String applyAll() {
                val customStr = "custom_rand('" + randStr() + "')";
                this.custom.accept(customStr);
                this.xdg.accept("*unexpected_xdg*");
                this.defaultWindows.accept("*unexpected_windows*");
                this.defaultUnix.accept("*unexpected_unix*");
                return customStr;
            }

            public String applyXdgAndDefaults() {
                val xdgStr = "xdg_rand('" + randStr() + "')";
                this.xdg.accept(xdgStr);
                this.defaultWindows.accept("*unexpected_windows*");
                this.defaultUnix.accept("*unexpected_unix*");
                return xdgStr;
            }

            public String applyBothDefaults() {
                val fallbackStr = "default_rand('" + randStr() + "')";
                this.defaultUnix.accept(fallbackStr);
                this.defaultWindows.accept(fallbackStr);
                return fallbackStr;
            }

            public String applyWindowsDefault() {
                val fallbackStr = "windows_rand('" + randStr() + "')";
                this.defaultWindows.accept(fallbackStr);
                this.defaultUnix.accept("*unexpected_unix*");
                return fallbackStr;
            }

            public String applyUnixDefault() {
                val fallbackStr = "unix_rand('" + randStr() + "')";
                this.defaultUnix.accept(fallbackStr);
                this.defaultWindows.accept("*unexpected_windows*");
                return fallbackStr;
            }

            private String randStr() {
                return Arbitraries.strings().ofMaxLength(16).alpha().sample();
            }
        }
    }
}
