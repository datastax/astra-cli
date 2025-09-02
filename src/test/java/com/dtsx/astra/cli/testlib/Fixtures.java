package com.dtsx.astra.cli.testlib;

import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RoleRef;
import com.dtsx.astra.cli.utils.JsonUtils;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.org.domain.CreateTokenResponse;
import com.dtsx.astra.sdk.org.domain.IamToken;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.org.domain.Role;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import lombok.val;

import java.io.File;
import java.util.List;

public abstract class Fixtures {
    public static class Databases {
        public static final List<Database> Many = load("Databases.json", new TypeReference<>() {});

        public static final Database One = Many.stream().filter(db -> db.getInfo().getName().equals("region_test")).findFirst().orElseThrow();

        public static final DbRef NameRef = DbRef.fromNameUnsafe(One.getInfo().getName());
    }

    public static class Roles {
        public static final List<Role> Many = load("Roles.json", new TypeReference<>() {});

        public static final Role One = Many.getFirst();

        public static final RoleRef NameRef = RoleRef.fromNameUnsafe(One.getName());
    }

    public static class Tokens {
        public static final CreateTokenResponse Created = load("CreatedAstraToken.json", new TypeReference<>() {});

        public static final List<IamToken> Infos = load("AstraTokenInfos.json", new TypeReference<>() {});

        public static final AstraToken One = AstraToken.mkUnsafe(Created.getToken());
    }

    public static final Organization Organization = load("Organization.json", new TypeReference<>() {});

    @SneakyThrows
    private static <T> T load(String resource, TypeReference<T> type) {
        try (val is = Fixtures.class.getClassLoader().getResourceAsStream("serialized-fixtures" + File.separator + resource)) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: " + resource);
            }

            return JsonUtils.objectMapper().readValue(is, type);
        }
    }
}
