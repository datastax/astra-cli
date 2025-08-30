package com.dtsx.astra.cli.testlib;

import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.utils.JsonUtils;
import lombok.SneakyThrows;

import java.io.File;

public abstract class Fixtures {
    public static final Database Database = load("Database.json", Database.class);

    @SneakyThrows
    private static <T> T load(String resource, Class<T> clazz) {
        try (var is = Fixtures.class.getResourceAsStream("serialized-fixtures" + File.separator + resource)) {
            return JsonUtils.getObjectMapper().readValue(is, clazz);
        }
    }
}
