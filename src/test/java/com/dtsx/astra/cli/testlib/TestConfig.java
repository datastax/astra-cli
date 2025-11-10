package com.dtsx.astra.cli.testlib;

import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

public class TestConfig {
    private static final Dotenv dotenv = Dotenv.load();
    private static final Pattern DB_COMPONENTS_REGEX = Pattern.compile("^([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})-([a-z0-9_-]+)\\.apps\\.astra(?:-(dev|test))?\\.datastax\\.com", Pattern.CASE_INSENSITIVE);

    public static AstraToken token() {
        return AstraToken.parse(env("ASTRA_TOKEN")).getRight();
    }

    public static AstraEnvironment env() {
        return AstraEnvironment.valueOf(env("ASTRA_ENV", "prod").toUpperCase());
    }

    public static String apiEndpoint() {
        return env("ASTRA_DB_URL");
    }

    public static DbRef dbId() {
        val hostname = apiEndpoint().replaceFirst("https?://", "");
        val matcher = DB_COMPONENTS_REGEX.matcher(hostname);

        return DbRef.fromId(UUID.fromString(
            matcher.find()
                ? matcher.group(1).toLowerCase()
                : "*error*"
        ));
    }

    public static RegionName dbRegion() {
        val hostname = apiEndpoint().replaceFirst("https?://", "");
        val matcher = DB_COMPONENTS_REGEX.matcher(hostname);

        return RegionName.mkUnsafe(
            matcher.find()
                ? matcher.group(2).toLowerCase()
                : "*error*"
        );
    }

    public static String astraHome() {
        return env("ASTRA_HOME", ".cli_tests_temp/java");
    }

    public static Path astraHome(FileSystem fs) {
        return fs.getPath(astraHome());
    }

    public static Profile profile() {
        return new Profile(Optional.empty(), token(), env(), Optional.empty());
    }

    private static String env(String key) {
        val env = env(key, null);

        if (env == null) {
            throw new IllegalStateException("Environment variable " + key + " is not set");
        }

        return env;
    }

    private static String env(String key, @Nullable String defaultValue) {
        return Optional.ofNullable(dotenv.get(key))
            .filter(s -> !s.isBlank())
            .orElse(defaultValue);
    }
}
