package com.dtsx.astra.sdk.utils;

import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.OptionValidationException;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Optional;

import static com.dtsx.astra.cli.core.output.ExitCode.VALIDATION_ISSUE;

@Getter
@Accessors(fluent = false)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "name")
public final class AstraEnvironment {
    public static final AstraEnvironment PROD = new AstraEnvironment(
        0,
        "PROD",
        "https://api.astra.datastax.com/v2",
        ".apps.astra.datastax.com",
        ".api.streaming.datastax.com"
    );

    public static final AstraEnvironment DEV = new AstraEnvironment(
        1,
        "DEV",
        "https://api.dev.cloud.datastax.com/v2",
        ".apps.astra-dev.datastax.com",
        ".api.dev.streaming.datastax.com"
    );

    public static final AstraEnvironment TEST = new AstraEnvironment(
        2,
        "TEST",
        "https://api.test.cloud.datastax.com/v2",
        ".apps.astra-test.datastax.com",
        ".api.staging.streaming.datastax.com"
    );

    public static final String LOCAL_NAME = "LOCAL";

    @Accessors(fluent = true)
    private final int ordinal;

    @Accessors(fluent = true)
    private final String name;

    private final String endPoint;
    private final String appsSuffix;
    private final String streamingV3Suffix;

    public boolean isLocal() {
        return this.ordinal == -1;
    }

    public static AstraEnvironment local(String endpoint) {
        return new AstraEnvironment(-1, LOCAL_NAME, endpoint, "n/a", "n/a"); // TODO figure out what to do with these endpoints
    }

    public static AstraEnvironment[] values() {
        return new AstraEnvironment[]{ PROD, DEV, TEST };
    }

    public static String[] allValuesLower() {
        return new String[]{ PROD.name.toLowerCase(), DEV.name.toLowerCase(), TEST.name.toLowerCase(), LOCAL_NAME.toLowerCase() };
    }

    public static AstraEnvironment valueOf(String name) {
        for (val env : values()) {
            if (env.name.equals(name.toUpperCase())) {
                return env;
            }
        }
        throw new IllegalArgumentException("No AstraEnvironment constant with name: " + name);
    }

    public static AstraEnvironment resolve(Optional<String> envString, Optional<String> localEndpoint) {
        val envName = envString.orElse(PROD.name()).toUpperCase();

        if (envName.equalsIgnoreCase(LOCAL_NAME)) {
            if (localEndpoint.isEmpty()) {
                throw new AstraCliException(VALIDATION_ISSUE, "@|bold,red When using --env local, you must provide --local-endpoint with the URL of your local DevOps API instance.|@");
            }
            return AstraEnvironment.local(localEndpoint.get());
        }

        try {
            return AstraEnvironment.valueOf(envName);
        } catch (IllegalArgumentException e) {
            throw new OptionValidationException("env", "Invalid environment: '" + envName + "'. Expected one of: " + String.join(", ", AstraEnvironment.allValuesLower()));
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
