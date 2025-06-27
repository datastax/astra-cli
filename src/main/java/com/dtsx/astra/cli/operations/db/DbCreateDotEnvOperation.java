package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.config.AstraConfig.Profile;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.exceptions.db.KeyspaceNotFoundException;
import com.dtsx.astra.cli.core.exceptions.db.RegionNotFoundException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.core.parsers.env.EnvFile;
import com.dtsx.astra.cli.core.parsers.env.EnvFile.EnvComment;
import com.dtsx.astra.cli.core.parsers.env.EnvFile.EnvKVPair;
import com.dtsx.astra.cli.core.parsers.env.EnvParseException;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.utils.ApiLocator;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.dtsx.astra.cli.operations.db.DbCreateDotEnvOperation.EnvKey.*;
import static com.dtsx.astra.cli.operations.db.DbCreateDotEnvOperation.*;

@RequiredArgsConstructor
public class DbCreateDotEnvOperation implements Operation<CreateDotEnvResult> {
    private static final File DEFAULT_ENV_FILE = new File(".env");

    private final DbGateway dbGateway;
    private final OrgGateway orgGateway;
    private final CreateDotEnvRequest request;

    public record CreateDotEnvRequest(
        Profile profile,
        DbRef dbRef,
        Optional<KeyspaceRef> ksRef,
        Optional<RegionName> region,
        Optional<File> file,
        boolean print,
        Set<EnvKey> keys,
        Optional<Boolean> overwrite,
        Function<Set<String>, Boolean> askIfShouldOverwrite
    ) {}

    public sealed interface CreateDotEnvResult {}

    public record CreatedDotEnvFile(File file) implements CreateDotEnvResult {}
    public record UpdatedDotEnvFile(File file, boolean overwritten) implements CreateDotEnvResult {}
    public record CreatedDotEnvContent(EnvFile content) implements CreateDotEnvResult {}
    public record NothingToUpdate(File file) implements CreateDotEnvResult {}

    @Override
    public CreateDotEnvResult execute() {
        val source = resolveSourceContent(request.file(), request.print());

        val shouldOverwrite = shouldOverwrite(source, request);

        val wasUpdated = appendToEnvFile(source, request, shouldOverwrite);

        if (request.print) {
            return new CreatedDotEnvContent(source);
        }

        val outputFile = request.file().orElse(DEFAULT_ENV_FILE);
        val fileAlreadyExists = outputFile.exists();

        if (wasUpdated) {
            source.writeToFile(outputFile);

            if (fileAlreadyExists) {
                return new UpdatedDotEnvFile(outputFile, shouldOverwrite);
            } else {
                return new CreatedDotEnvFile(outputFile);
            }
        } else {
            return new NothingToUpdate(outputFile);
        }
    }

    public enum EnvKey {
        ASTRA_ORG_ID,
        ASTRA_ORG_NAME,
        ASTRA_ORG_TOKEN,

        ASTRA_DB_ID,
        ASTRA_DB_REGION,
        ASTRA_DB_KEYSPACE,
        ASTRA_DB_APPLICATION_TOKEN,
        ASTRA_DB_ENVIRONMENT,

        ASTRA_DB_SECURE_BUNDLE_PATH,
        ASTRA_DB_SECURE_BUNDLE_URL,

        ASTRA_DB_GRAPHQL_URL,
        ASTRA_DB_GRAPHQL_URL_PLAYGROUND,
        ASTRA_DB_GRAPHQL_URL_SCHEMA,
        ASTRA_DB_GRAPHQL_URL_ADMIN,

        ASTRA_DB_API_ENDPOINT,
        ASTRA_DB_API_ENDPOINT_SWAGGER,

        ASTRA_DB_REST_URL,
        ASTRA_DB_REST_URL_SWAGGER,

        ASTRA_STREAMING_NAME,
        ASTRA_STREAMING_CLOUD,
        ASTRA_STREAMING_REGION,
        ASTRA_STREAMING_PULSAR_TOKEN,
        ASTRA_STREAMING_BROKER_URL,
        ASTRA_STREAMING_WEBSERVICE_URL,
        ASTRA_STREAMING_WEBSOCKET_URL,
    }

    private EnvFile resolveSourceContent(Optional<File> file, boolean print) {
        if (print && file.isEmpty()) {
            return new EnvFile(new ArrayList<>());
        }

        val envFile = file.orElse(DEFAULT_ENV_FILE);

        try {
            return EnvFile.readEnvFile(envFile);
        } catch (FileNotFoundException e) {
            return new EnvFile(new ArrayList<>());
        } catch (EnvParseException e) {
            throw new EnvParseExceptionWrapper(e, envFile);
        }
    }

    private boolean shouldOverwrite(EnvFile source, CreateDotEnvRequest request) {
        if (request.overwrite.isPresent()) {
            return request.overwrite.get();
        }

        val reqKeys = request.keys.stream().map(EnvKey::name).toList();

        val diff = source.getVariables().stream()
            .map(EnvKVPair::key)
            .filter(reqKeys::contains)
            .collect(Collectors.toSet());

        return diff.isEmpty() || request.askIfShouldOverwrite.apply(diff);
    }

    private boolean appendToEnvFile(EnvFile source, CreateDotEnvRequest request, boolean overwrite) {
        val envSetter = new EnvSetter(source, request, overwrite);
        var wasSet = false;

        source.filterNodes(node -> !(node instanceof EnvComment(List<String> comments) &&
            comments.stream().anyMatch(c -> c.contains("Generated by Astra CLI"))));

        source.appendComment("# Generated by Astra CLI for database " + request.dbRef);

        {
            wasSet |= envSetter.set(ASTRA_ORG_ID, () -> org().getId());
            wasSet |= envSetter.set(ASTRA_ORG_NAME, () -> org().getName());
            wasSet |= envSetter.set(ASTRA_ORG_TOKEN, request.profile.token()::unwrap);
        }

        {
            wasSet |= envSetter.set(ASTRA_DB_ID, () -> db(request).getId());
            wasSet |= envSetter.set(ASTRA_DB_REGION, () -> resolveRegion(request).unwrap());
            wasSet |= envSetter.set(ASTRA_DB_KEYSPACE, () -> resolveKeyspace(request));
            wasSet |= envSetter.set(ASTRA_DB_APPLICATION_TOKEN, request.profile.token()::unwrap);
            wasSet |= envSetter.set(ASTRA_DB_ENVIRONMENT, () -> request.profile.env().name().toLowerCase());
        }

        {
            wasSet |= envSetter.set(ASTRA_DB_SECURE_BUNDLE_PATH, () -> downloadAndResolveScbPath(request));
            wasSet |= envSetter.set(ASTRA_DB_SECURE_BUNDLE_URL, () -> resolveScbUrl(request));
        }

        {
            wasSet |= envSetter.set(ASTRA_DB_GRAPHQL_URL, () -> resolveGqlEndpoint(request) + "/graphql/" + resolveKeyspace(request));
            wasSet |= envSetter.set(ASTRA_DB_GRAPHQL_URL_PLAYGROUND, () -> resolveGqlEndpoint(request) + "/playground");
            wasSet |= envSetter.set(ASTRA_DB_GRAPHQL_URL_SCHEMA, () -> resolveGqlEndpoint(request) + "graphql-schema");
            wasSet |= envSetter.set(ASTRA_DB_GRAPHQL_URL_ADMIN, () -> resolveGqlEndpoint(request) + "graphql-admin");
        }

        {
            wasSet |= envSetter.set(ASTRA_DB_API_ENDPOINT, () -> resolveDataApiEndpoint(request, ""));
            wasSet |= envSetter.set(ASTRA_DB_API_ENDPOINT_SWAGGER, () -> resolveDataApiEndpoint(request, "/api/json/swagger-ui/"));
        }

        {
            wasSet |= envSetter.set(ASTRA_DB_REST_URL, () -> resolveRestApiEndpoint(request, ""));
            wasSet |= envSetter.set(ASTRA_DB_REST_URL_SWAGGER, () -> resolveRestApiEndpoint(request, "/swagger-ui/"));
        }

        return wasSet;
    }

    private @Nullable Organization cachedOrg;
    private @Nullable Database cachedDb;
    private @Nullable RegionName cachedRegion;
    private @Nullable String cachedKeyspace;

    private Organization org() {
        if (cachedOrg == null) {
            cachedOrg = orgGateway.getCurrentOrg();
        }
        return cachedOrg;
    }

    private Database db(CreateDotEnvRequest request) {
        if (cachedDb == null) {
            cachedDb = dbGateway.findOneDb(request.dbRef);
        }
        return cachedDb;
    }

    private RegionName resolveRegion(CreateDotEnvRequest request) {
        if (cachedRegion == null) {
            cachedRegion = request.region
                .map(r -> db(request).getInfo().getDatacenters().stream()
                    .map(Datacenter::getRegion)
                    .filter(region -> region.equalsIgnoreCase(r.unwrap()))
                    .findFirst()
                    .map(RegionName::mkUnsafe)
                    .orElseThrow(() -> new RegionNotFoundException(request.dbRef, r))
                )
                .orElseGet(() -> RegionName.mkUnsafe(db(request).getInfo().getRegion()));
        }
        return cachedRegion;
    }

    private String resolveKeyspace(CreateDotEnvRequest request) {
        if (cachedKeyspace == null) {
            cachedKeyspace = request.ksRef
                .map(ks -> db(request).getInfo().getKeyspaces().stream()
                    .filter(ks.name()::equalsIgnoreCase)
                    .findFirst()
                    .orElseThrow(() -> new KeyspaceNotFoundException(ks))
                )
                .orElseGet(() -> db(request).getInfo().getKeyspace());
        }
        return cachedKeyspace;
    }

    private String downloadAndResolveScbPath(CreateDotEnvRequest request) {
        val dbName = db(request).getInfo().getName();
        val datacenter = resolveDatacenter(request);

        val paths = dbGateway.downloadCloudSecureBundles(request.dbRef, dbName, List.of(datacenter));

        if (paths.size() != 1) {
            throw new CongratsYouFoundABugException("Expected exactly one SCB path, but got %d: %s".formatted(paths.size(), paths));
        }

        return paths.getFirst();
    }

    private String resolveScbUrl(CreateDotEnvRequest request) {
        return resolveDatacenter(request).getSecureBundleUrl();
    }

    private String resolveGqlEndpoint(CreateDotEnvRequest request) {
        return ApiLocator.getApiGraphQLEndPoint(request.profile.env(), db(request).getId(), resolveRegion(request).unwrap());
    }

    private String resolveDataApiEndpoint(CreateDotEnvRequest request, String suffix) {
        return (db(request).getInfo().getDbType() != null)
            ? ApiLocator.getApiEndpoint(request.profile.env(), db(request).getId(), resolveRegion(request).unwrap()) + suffix
            : "";
    }

    private String resolveRestApiEndpoint(CreateDotEnvRequest request, String suffix) {
        return (db(request).getInfo().getDbType() == null)
            ? ApiLocator.getApiRestEndpoint(request.profile.env(), db(request).getId(), resolveRegion(request).unwrap()) + suffix
            : "";
    }

    private Datacenter resolveDatacenter(CreateDotEnvRequest request) {
        return db(request).getInfo().getDatacenters().stream()
            .filter(dc -> dc.getRegion().equalsIgnoreCase(resolveRegion(request).unwrap()))
            .findFirst()
            .orElseThrow(() -> new RegionNotFoundException(request.dbRef, resolveRegion(request)));
    }

    private record EnvSetter(EnvFile envFile, CreateDotEnvRequest request, boolean overwrite) {
        public boolean set(EnvKey key, Supplier<String> valueSupplier) {
            if (!request.keys.contains(key)) {
                return false;
            }

            if (envFile.lookupKey(key.name()).isPresent() && overwrite) {
                envFile.deleteVariable(key.name());
            }

            envFile.appendVariable(key.name(), valueSupplier.get());
            return true;
        }
    }

    public static class EnvParseExceptionWrapper extends AstraCliException {
        public EnvParseExceptionWrapper(EnvParseException e, File file) {
            super("@|bold,red An error occurred parsing the .env file '%s':|@%n%n%s".formatted(file.getAbsolutePath(), e.getMessage()));
        }
    }
}
