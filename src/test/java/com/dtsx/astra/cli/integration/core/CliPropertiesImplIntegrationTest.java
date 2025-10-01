package com.dtsx.astra.cli.integration.core;

import com.dtsx.astra.cli.core.models.Version;
import com.dtsx.astra.cli.core.properties.CliEnvironment;
import com.dtsx.astra.cli.core.properties.CliEnvironmentImpl;
import com.dtsx.astra.cli.core.properties.CliProperties;
import com.dtsx.astra.cli.core.properties.CliPropertiesImpl;
import lombok.val;
import net.jqwik.api.Arbitraries;
import org.jetbrains.annotations.Nullable;
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
public class CliPropertiesImplIntegrationTest {
    private final CliEnvironment cliEnv = new CliEnvironmentImpl();

    private @Nullable CliProperties cachedProps = null;

    private CliProperties cliProps() {
        if (cachedProps == null) {
            cachedProps = CliPropertiesImpl.mkAndLoadSysProps(cliEnv);
        }
        return cachedProps;
    }

    @Nested
    public class external_software {
        @Test
        public void cqlsh(SystemProperties systemProps) {
            systemProps.set(
                "cqlsh.url", "<cqlsh_url>",
                "cqlsh.version", "v3.2.1"
            );

            val cqlsh = cliProps().cqlsh();

            assertThat(cqlsh.url()).isEqualTo("<cqlsh_url>");
            assertThat(cqlsh.version()).isEqualTo(Version.mkUnsafe("v3.2.1"));
        }

        @Test
        public void dsbulk(SystemProperties systemProps) {
            systemProps.set(
                "dsbulk.url", "<dsbulk_url>",
                "dsbulk.version", "v6.5.4"
            );

            val dsbulk = cliProps().dsbulk();

            assertThat(dsbulk.url()).isEqualTo("<dsbulk_url>");
            assertThat(dsbulk.version()).isEqualTo(Version.mkUnsafe("6.5.4"));
        }

        @Test
        public void pulsar(SystemProperties systemProps) {
            systemProps.set(
                "pulsar-shell.url", "<pulsar_url>",
                "pulsar-shell.version", "9.8.7"
            );

            val pulsar = cliProps().pulsar();

            assertThat(pulsar.url()).isEqualTo("<pulsar_url>");
            assertThat(pulsar.version()).isEqualTo(Version.mkUnsafe("9.8.7"));
        }
    }

    @Nested
    public class versioning {
        @Test
        public void static_version(SystemProperties systemProps) {
            systemProps.set(
                "cli.version", "v3.3.3"
            );
            assertThat(cliProps().version()).isEqualTo(Version.mkUnsafe("3.3.3"));
        }
    }

    @Nested
    public class file_names {
        @Test
        public void rc_file_name(SystemProperties systemProps) {
            systemProps.set(
                "cli.rc-file.name", ".astrarc"
            );
            assertThat(cliProps().rcFileName()).isEqualTo(".astrarc");
        }

        @Test
        public void home_folder_name(SystemProperties systemProps) {
            systemProps.set(
                "cli.home-folder.name", "astra"
            );
            assertThat(cliProps().homeFolderName(false)).isEqualTo("astra");
            assertThat(cliProps().homeFolderName(true)).isEqualTo("." + cliProps().homeFolderName(false));
        }
    }

    @Nested
    public class env_vars {
        @Test
        public void rc_env_var(SystemProperties systemProps) {
            systemProps.set(
                "cli.rc-file.env-var", "CUSTOM_ASTRARC_ENV_VAR"
            );
            System.out.println(System.getProperty("cli.rc-file.env-var") + "1");
            assertThat(cliProps().rcEnvVar()).isEqualTo("CUSTOM_ASTRARC_ENV_VAR");
        }

        @Test
        public void home_env_var(SystemProperties systemProps) {
            systemProps.set(
                "cli.home-folder.env-var", "CUSTOM_ASTRA_HOME_ENV_VAR"
            );
            assertThat(cliProps().homeEnvVar()).isEqualTo("CUSTOM_ASTRA_HOME_ENV_VAR");
        }
    }

    @Nested
    public class file_paths { // unfortunately can't use jqwik b/c it doesn't work w/. system-stubs
        private final BiFunction<SystemProperties, EnvironmentVariables, Steps> mkRcFileSteps = (sys, env) -> new Steps(
            (path) -> { sys.set("cli.rc-file.env-var", "CUSTOM_RC_PATH"); env.set(cliProps().rcEnvVar(), path); },
            (path) -> env.set("XDG_CONFIG_HOME", path),
            (path) -> sys.set("user.home", path),
            (path) -> sys.set("user.home", path)
        );

        private final BiFunction<SystemProperties, EnvironmentVariables, Steps> mkHomeFolderSteps = (sys, env) -> new Steps(
            (path) -> { sys.set("cli.home-folder.env-var", "CUSTOM_HOME_PATH"); env.set(cliProps().homeEnvVar(), path); },
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
                assertThat(cliProps().rcFileLocations(true)).isEqualTo(customPath);
                assertThat(System.getProperty("cli.rc-file.path")).isEqualTo(customPath);

                assertThat(cliProps().rcFileLocations(false)).isEqualTo(customPath);
                assertThat(System.getProperty("cli.rc-file.path")).isEqualTo(customPath);
            }

            @Test
            public void uses_xdg_if_no_custom_path(SystemProperties sys, EnvironmentVariables env) {
                sys.set("cli.home-folder.name", "<home_folder_name>");
                val xdgPath = mkRcFileSteps.apply(sys, env).applyXdgAndDefaults();

                val expectedSubpath = File.separator + "<home_folder_name>" + File.separator + cliProps().rcFileName();

                // returned path should not depend on the os; the display path should depend on the os though
                assertThat(cliProps().rcFileLocations(true)).isEqualTo(xdgPath + expectedSubpath);
                assertThat(System.getProperty("cli.rc-file.path")).isEqualTo("%XDG_CONFIG_HOME%" + expectedSubpath);

                assertThat(cliProps().rcFileLocations(false)).isEqualTo(xdgPath + expectedSubpath);
                assertThat(System.getProperty("cli.rc-file.path")).isEqualTo("$XDG_CONFIG_HOME" + expectedSubpath);
            }

            @Test
            public void defaults_to_user_home_if_no_path_specified(SystemProperties sys, EnvironmentVariables env) {
                val defaultPath = mkRcFileSteps.apply(sys, env).applyBothDefaults();

                sys.set("cli.rc-file.name", "custom-rc-file");
                val expectedSubpath = File.separator + "custom-rc-file";

                // returned path should not depend on the os; the display path should depend on the os though
                assertThat(cliProps().rcFileLocations(true)).isEqualTo(defaultPath + expectedSubpath);
                assertThat(System.getProperty("cli.rc-file.path")).isEqualTo("%USERPROFILE%" + expectedSubpath);

                assertThat(cliProps().rcFileLocations(false)).isEqualTo(defaultPath + expectedSubpath);
                assertThat(System.getProperty("cli.rc-file.path")).isEqualTo("~" + expectedSubpath);
            }
        }

        @Nested
        public class home_folder_path {
            @Test
            public void prioritizes_custom_path(SystemProperties sys, EnvironmentVariables env) {
                val customPath = mkHomeFolderSteps.apply(sys, env).applyAll();

                assertThat(cliProps().homeFolderLocations(true)).isEqualTo(customPath);
                assertThat(System.getProperty("cli.home-folder.path")).isEqualTo(customPath);

                assertThat(cliProps().homeFolderLocations(false)).isEqualTo(customPath);
                assertThat(System.getProperty("cli.home-folder.path")).isEqualTo(customPath);
            }

            @Test
            public void uses_xdg_if_no_custom_path(SystemProperties sys, EnvironmentVariables env) {
                val xdgPath = mkHomeFolderSteps.apply(sys, env).applyXdgAndDefaults();

                sys.set("cli.home-folder.name", "custom-home-folder");
                val expectedSubpath = File.separator + "custom-home-folder";

                // returned path should not depend on the os; the display path should depend on the os though
                assertThat(cliProps().homeFolderLocations(true)).isEqualTo(xdgPath + expectedSubpath);
                assertThat(System.getProperty("cli.home-folder.path")).isEqualTo("%XDG_DATA_HOME%" + expectedSubpath);

                assertThat(cliProps().homeFolderLocations(false)).isEqualTo(xdgPath + expectedSubpath);
                assertThat(System.getProperty("cli.home-folder.path")).isEqualTo("$XDG_DATA_HOME" + expectedSubpath);
            }

            @Test
            public void defaults_to_user_home_if_no_path_specified_on_unix(SystemProperties sys, EnvironmentVariables env) {
                val defaultPath = mkHomeFolderSteps.apply(sys, env).applyUnixDefault();

                sys.set("cli.home-folder.name", "custom-home-folder");
                val expectedSubpath = File.separator + ".custom-home-folder";

                assertThat(cliProps().homeFolderLocations(false)).isEqualTo(defaultPath + expectedSubpath);
                assertThat(System.getProperty("cli.home-folder.path")).isEqualTo("~" + expectedSubpath);
            }

            @Test
            public void defaults_to_localappdata_if_no_path_specified_on_windows(SystemProperties sys, EnvironmentVariables env) {
                val defaultPath = mkHomeFolderSteps.apply(sys, env).applyWindowsDefault();

                sys.set("cli.home-folder.name", "custom-home-folder");
                val expectedSubpath = File.separator + ".custom-home-folder";

                assertThat(cliProps().homeFolderLocations(true)).isEqualTo(defaultPath + expectedSubpath);
                assertThat(System.getProperty("cli.home-folder.path")).isEqualTo("%LOCALAPPDATA%" + expectedSubpath);
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
                this.custom.accept("");
                this.xdg.accept(xdgStr);
                this.defaultWindows.accept("*unexpected_windows*");
                this.defaultUnix.accept("*unexpected_unix*");
                return xdgStr;
            }

            public String applyBothDefaults() {
                val fallbackStr = "default_rand('" + randStr() + "')";
                this.custom.accept("");
                this.xdg.accept("");
                this.defaultUnix.accept(fallbackStr);
                this.defaultWindows.accept(fallbackStr);
                return fallbackStr;
            }

            public String applyWindowsDefault() {
                val fallbackStr = "windows_rand('" + randStr() + "')";
                this.custom.accept("");
                this.xdg.accept("");
                this.defaultWindows.accept(fallbackStr);
                this.defaultUnix.accept("*unexpected_unix*");
                return fallbackStr;
            }

            public String applyUnixDefault() {
                val fallbackStr = "unix_rand('" + randStr() + "')";
                this.custom.accept("");
                this.xdg.accept("");
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
