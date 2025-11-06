package com.dtsx.astra.cli.testlib;

import com.datastax.astra.client.collections.definition.CollectionDefinition;
import com.datastax.astra.client.collections.definition.CollectionDescriptor;
import com.datastax.astra.client.databases.commands.results.FindEmbeddingProvidersResult;
import com.datastax.astra.client.tables.definition.TableDefinition;
import com.datastax.astra.client.tables.definition.TableDescriptor;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.config.ProfileName;
import com.dtsx.astra.cli.core.models.*;
import com.dtsx.astra.cli.utils.JsonUtils;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import com.dtsx.astra.sdk.org.domain.*;
import com.dtsx.astra.sdk.streaming.domain.CdcDefinition;
import com.dtsx.astra.sdk.streaming.domain.Tenant;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.SneakyThrows;
import lombok.val;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.datastax.astra.client.core.options.DataAPIClientOptions.DEFAULT_KEYSPACE;

public abstract class Fixtures {
    public static class Databases {
        public static final List<Database> Many = load("Databases.json", new TypeReference<>() {});

        public static final Database One = Many.stream().filter(db -> db.getInfo().getName().equals("two_regions_db")).findFirst().orElseThrow();

        public static final DbRef NameRef = DbRef.fromNameUnsafe(One.getInfo().getName());

        public static final DbRef IdRef = DbRef.fromId(UUID.fromString(One.getId()));

        public static final KeyspaceRef Keyspace = KeyspaceRef.mkUnsafe(Databases.NameRef, DEFAULT_KEYSPACE);

        public static final CloudProvider Cloud = CloudProvider.fromSdkType(One.getInfo().getCloudProvider());
    }

    public static class Tenants {
        public static final List<Tenant> Many = load("Tenants.json", new TypeReference<>() {});

        public static final Tenant One = Many.getFirst();

        public static final TenantName Name = TenantName.mkUnsafe(One.getTenantName());
    }

    public static class Users {
        public static final List<User> Many = load("Users.json", new TypeReference<>() {});

        public static final User One = Many.getFirst();

        public static final UserRef EmailRef = UserRef.fromEmailUnsafe(One.getEmail());
    }

    public static class Roles {
        public static final List<Role> Many = load("Roles.json", new TypeReference<>() {});

        public static final Role One = Many.getFirst();

        public static final RoleRef NameRef = RoleRef.fromNameUnsafe(One.getName());
    }

    public static class Cdc {
        public static final List<CdcDefinition> Many = load("CdcDefinitions.json", new TypeReference<>() {});

        public static final CdcDefinition One = Many.getFirst();

        public static final CdcRef Ref = CdcRef.fromDefinition(
            TableRef.mkUnsafe(KeyspaceRef.mkUnsafe(DbRef.fromNameUnsafe(One.getDatabaseName()), One.getKeyspace()), One.getDatabaseTable()),
            TenantName.mkUnsafe(One.getTenant())
        );
    }

    public static class Collections {
        public static final List<CollectionDescriptor> Many = load("CollectionDescriptors.json", new TypeReference<>() {});

        public static final CollectionDefinition One = Many.getFirst().getOptions();

        public static final CollectionRef Ref = CollectionRef.mkUnsafe(Databases.Keyspace, Many.getFirst().getName());
    }

    public static class Tables {
        public static final List<TableDescriptor> Many = load("TableDescriptors.json", new TypeReference<>() {});

        public static final TableDefinition One = Many.getFirst().getDefinition();

        public static final TableRef Ref = TableRef.mkUnsafe(Databases.Keyspace, Many.getFirst().getName());
    }

    public static class Regions {
        public static final List<Datacenter> DATACENTERS = Databases.One.getInfo().getDatacenters().stream().sorted(Comparator.comparing(Datacenter::getId)).toList();

        public static final Datacenter ONE = DATACENTERS.getFirst();

        public static final RegionName NAME = RegionName.mkUnsafe(ONE.getRegion());
    }

    public static final CreateTokenResponse CreateTokenResponse = load("CreateTokenResponse.json", new TypeReference<>() {});

    public static final FindEmbeddingProvidersResult FindEmbeddingProvidersResult = load("FindEmbeddingProvidersResult.json", new TypeReference<>() {});

    public static final AstraToken Token = AstraToken.mkUnsafe(CreateTokenResponse.getToken());

    public static final List<IamToken> TokenInfos = load("IamTokens.json", new TypeReference<>() {});

    public static final Organization Organization = load("Organization.json", new TypeReference<>() {});

    public static final Profile Profile = new Profile(
        Optional.of(ProfileName.mkUnsafe("*name*")),
        Fixtures.Token,
        AstraEnvironment.DEV
    );

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
