package com.dtsx.astra.cli.testlib;

import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.utils.JsonUtils;
import lombok.SneakyThrows;
import lombok.val;

import java.io.File;

public abstract class Fixtures {
    public static final Database Database = load("Database.json", Database.class);

    public static final DbRef DatabaseName; static {
        DatabaseName = DbRef.fromNameUnsafe(Database.getInfo().getName());
    }

    @SneakyThrows
    private static <T> T load(String resource, Class<T> clazz) {
        try (val is = Fixtures.class.getClassLoader().getResourceAsStream("serialized-fixtures" + File.separator + resource)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resource);
            }

            return JsonUtils.getObjectMapper().readValue(is, clazz);
        }
    }
}
