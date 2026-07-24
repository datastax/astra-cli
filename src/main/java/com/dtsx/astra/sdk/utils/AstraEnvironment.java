package com.dtsx.astra.sdk.utils;

import lombok.*;
import lombok.experimental.Accessors;

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

    @Accessors(fluent = true)
    private final int ordinal;

    @Accessors(fluent = true)
    private final String name;

    private final String endPoint;
    private final String appsSuffix;
    private final String streamingV3Suffix;

    public static AstraEnvironment local(String endpoint) {
        return new AstraEnvironment(-1, "LOCAL", endpoint, "", "");
    }

    public static AstraEnvironment[] values() {
        return new AstraEnvironment[]{ PROD, DEV, TEST };
    }

    public static String[] allValuesLower() {
        return new String[]{ PROD.name.toLowerCase(), DEV.name.toLowerCase(), TEST.name.toLowerCase(), "local" };
    }

    public static AstraEnvironment valueOf(String name) {
        for (val env : values()) {
            if (env.name.equals(name)) {
                return env;
            }
        }
        throw new IllegalArgumentException("No AstraEnvironment constant with name: " + name);
    }

    @Override
    public String toString() {
        return name;
    }
}
