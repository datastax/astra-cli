package com.dtsx.astra.cli.operations.dotenv;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.config.Profile;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.db.KeyspaceNotFoundException;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.KeyspaceRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.core.parsers.env.EnvFile;
import com.dtsx.astra.cli.core.parsers.env.EnvParseException;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.utils.DbUtils;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import com.dtsx.astra.sdk.org.domain.Organization;
import com.dtsx.astra.sdk.utils.ApiLocator;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.PARSE_ISSUE;
import static com.dtsx.astra.cli.operations.dotenv.DotEnvOperation.DotEnvResult;
import static com.dtsx.astra.cli.operations.dotenv.EnvKey.ASTRA_DB_SECURE_BUNDLE_PATH;

@RequiredArgsConstructor
public class DotEnvOperation implements Operation<DotEnvResult> {
    private final CliContext ctx;
    private final DbGateway dbGateway;
    private final OrgGateway orgGateway;
    private final DownloadsGateway downloadsGateway;
    private final DotEnvRequest request;

    public record DotEnvRequest(
        Profile profile,
        @Nullable DbRef dbRef,
        Optional<KeyspaceRef> ksRef,
        Optional<RegionName> region,
        Optional<Path> file,
        boolean print,
        Map<EnvKey, String> keys,
        Optional<Boolean> overwrite,
        Supplier<Set<EnvKey>> askForKeys,
        Supplier<DbRef> askForDbRef,
        Function<Set<String>, Boolean> askIfShouldOverwrite
    ) {}

    public sealed interface DotEnvResult {}

    public record CreatedDotEnvFile(Path file) implements DotEnvResult {}
    public record UpdatedDotEnvFile(Path file, boolean overwritten) implements DotEnvResult {}
    public record CreatedDotEnvContent(EnvFile content) implements DotEnvResult {}
    public record NothingToUpdate(Path file) implements DotEnvResult {}
    @Override
    public DotEnvResult execute() {
        val DEFAULT_ENV_FILE = ctx.path(".env");

        val source = resolveSourceContent(request.file(), request.print(), DEFAULT_ENV_FILE);

        val bindings = EnvKeysResolver.resolveBindings(source, request.keys, request.askForKeys);

        val resolvedDbRef = resolveDbRef(bindings);

        val scbPath = (bindings.containsValue(ASTRA_DB_SECURE_BUNDLE_PATH))
            ? downloadAndResolveScbPath(request, resolvedDbRef)
            : null;

        val resolvedValues = resolveValues(bindings, request, scbPath, resolvedDbRef);

        val shouldOverwrite = shouldOverwrite(source, resolvedValues, request);

        val wasUpdated = applyValues(source, bindings, resolvedValues, shouldOverwrite);

        if (request.print) {
            return new CreatedDotEnvContent(source);
        }

        val outputFile = request.file().orElse(DEFAULT_ENV_FILE);
        val fileAlreadyExists = Files.exists(outputFile);

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

    private EnvFile resolveSourceContent(Optional<Path> file, boolean print, Path defaultEnvFile) {
        if (print && file.isEmpty()) {
            return new EnvFile(new ArrayList<>());
        }

        val envFile = file.orElse(defaultEnvFile);

        try {
            return EnvFile.readFile(envFile);
        } catch (FileNotFoundException e) {
            return new EnvFile(new ArrayList<>());
        } catch (EnvParseException e) {
            throw new EnvParseExceptionWrapper(e, envFile);
        }
    }

    private @Nullable DbRef resolveDbRef(Map<String, EnvKey> bindings) {
        if (request.dbRef() != null) {
            return request.dbRef();
        }

        if (bindings.values().stream().anyMatch(EnvKey::needsDb)) {
            return request.askForDbRef().get();
        }
        return null;
    }

    private Map<String, String> resolveValues(Map<String, EnvKey> bindings, DotEnvRequest request, @Nullable Path scbPath, @Nullable DbRef dbRef) {
        val resolved = new LinkedHashMap<String, String>();

        bindings.forEach((key, env) -> resolved.put(key, switch (env) {
            case ASTRA_ORG_ID -> org().getId();
            case ASTRA_ORG_NAME -> org().getName();
            case ASTRA_ORG_TOKEN -> request.profile.token().unsafeUnwrap();

            case ASTRA_DB_ID -> db(dbRef).getId();
            case ASTRA_DB_NAME -> db(dbRef).getInfo().getName();
            case ASTRA_DB_REGION -> resolveRegion(request, dbRef).unwrap();
            case ASTRA_DB_KEYSPACE -> resolveKeyspace(request, dbRef);
            case ASTRA_DB_APPLICATION_TOKEN -> request.profile.token().unsafeUnwrap();
            case ASTRA_DB_ENVIRONMENT -> request.profile.env().name().toLowerCase();

            case ASTRA_DB_SECURE_BUNDLE_PATH -> Optional.ofNullable(scbPath).map(Path::toString).orElse(null);
            case ASTRA_DB_SECURE_BUNDLE_URL -> resolveScbUrl(request, dbRef);

            case ASTRA_DB_GRAPHQL_URL -> resolveGqlEndpoint(request, dbRef) + "/graphql/" + resolveKeyspace(request, dbRef);
            case ASTRA_DB_GRAPHQL_URL_PLAYGROUND -> resolveGqlEndpoint(request, dbRef) + "/playground";
            case ASTRA_DB_GRAPHQL_URL_SCHEMA -> resolveGqlEndpoint(request, dbRef) + "graphql-schema";
            case ASTRA_DB_GRAPHQL_URL_ADMIN -> resolveGqlEndpoint(request, dbRef) + "graphql-admin";

            case ASTRA_DB_API_ENDPOINT -> resolveDataApiEndpoint(request, dbRef, "");
            case ASTRA_DB_API_ENDPOINT_SWAGGER -> resolveDataApiEndpoint(request, dbRef, "/api/json/swagger-ui/");

            case ASTRA_DB_REST_URL -> resolveRestApiEndpoint(request, dbRef, "");
            case ASTRA_DB_REST_URL_SWAGGER -> resolveRestApiEndpoint(request, dbRef, "/swagger-ui/");
        }));

        return resolved;
    }

    private boolean shouldOverwrite(EnvFile source, Map<String, String> resolvedValues, DotEnvRequest request) {
        if (request.overwrite.isPresent()) {
            return request.overwrite.get();
        }

        val diff = new HashSet<String>();

        resolvedValues.forEach((key, newValue) ->
            source.lookupKey(key).ifPresent(existingValue -> {
                if (!existingValue.isBlank() && !existingValue.equals(newValue)) {
                    diff.add(key);
                }
            })
        );

        return diff.isEmpty() || request.askIfShouldOverwrite.apply(diff);
    }

    private boolean applyValues(EnvFile source, Map<String, EnvKey> bindings, Map<String, String> resolvedValues, boolean overwrite) {
        val wasUpdated = new Object() {
            boolean ref = false;
        };

        resolvedValues.forEach((key, value) -> {
            val existing = source.lookupKey(key);
            val hasValue = existing.isPresent() && !existing.get().isBlank();

            if (hasValue && !overwrite) {
                return;
            }

            if (source.updateVariable(key, value)) {
                wasUpdated.ref = true;
                return;
            }

            val env = bindings.get(key);
            source.appendNewLine();
            if (env != null && key.equals(env.name())) {
                source.appendComment("# Created by Astra CLI");
            } else if (env != null) {
                source.appendComment("# astra: " + env.name() + " - Created by Astra CLI");
            }

            source.appendVariable(key, value);
            wasUpdated.ref = true;
        });

        return wasUpdated.ref;
    }

    private @Nullable Organization cachedOrg;
    private @Nullable Database cachedDb;
    private @Nullable RegionName cachedRegion;
    private @Nullable String cachedKeyspace;

    private Organization org() {
        if (cachedOrg == null) {
            cachedOrg = orgGateway.current();
        }
        return cachedOrg;
    }

    private Database db(DbRef dbRef) {
        if (cachedDb == null) {
            cachedDb = dbGateway.findOne(dbRef);
        }
        return cachedDb;
    }

    private RegionName resolveRegion(DotEnvRequest request, DbRef dbRef) {
        if (cachedRegion == null) {
            cachedRegion = DbUtils.resolveRegionName(db(dbRef), request.region);
        }
        return cachedRegion;
    }

    private String resolveKeyspace(DotEnvRequest request, DbRef dbRef) {
        if (cachedKeyspace == null) {
            cachedKeyspace = request.ksRef
                .map(ks -> Optional.ofNullable(db(dbRef).getInfo().getKeyspaces()).orElse(Set.of()).stream()
                    .filter(ks.name()::equalsIgnoreCase)
                    .findFirst()
                    .orElseThrow(() -> new KeyspaceNotFoundException(ks))
                )
                .orElseGet(() -> db(dbRef).getInfo().getKeyspace());
        }
        return cachedKeyspace;
    }

    private Path downloadAndResolveScbPath(DotEnvRequest request, DbRef dbRef) {
        if (dbRef == null) {
            return null;
        }

        val datacenter = resolveDatacenter(request, dbRef);

        return downloadsGateway.downloadCloudSecureBundles(dbRef, List.of(datacenter)).getFirst();
    }

    private String resolveScbUrl(DotEnvRequest request, DbRef dbRef) {
        return resolveDatacenter(request, dbRef).getSecureBundleUrl();
    }

    private String resolveGqlEndpoint(DotEnvRequest request, DbRef dbRef) {
        return ApiLocator.getApiGraphQLEndPoint(request.profile.env(), db(dbRef).getId(), resolveRegion(request, dbRef).unwrap());
    }

    private String resolveDataApiEndpoint(DotEnvRequest request, DbRef dbRef, String suffix) {
        return (db(dbRef).getInfo().getDbType() != null)
            ? ApiLocator.getApiEndpoint(request.profile.env(), db(dbRef).getId(), resolveRegion(request, dbRef).unwrap()) + suffix
            : "";
    }

    private String resolveRestApiEndpoint(DotEnvRequest request, DbRef dbRef, String suffix) {
        return (db(dbRef).getInfo().getDbType() == null)
            ? ApiLocator.getApiRestEndpoint(request.profile.env(), db(dbRef).getId(), resolveRegion(request, dbRef).unwrap()) + suffix
            : "";
    }

    private Datacenter resolveDatacenter(DotEnvRequest request, DbRef dbRef) {
        return DbUtils.resolveDatacenter(db(dbRef), request.region);
    }

    public static class EnvParseExceptionWrapper extends AstraCliException {
        public EnvParseExceptionWrapper(EnvParseException e, Path file) {
            super(PARSE_ISSUE, "@|bold,red An error occurred parsing the .env file '%s':|@%n%n%s".formatted(file, e.getMessage()));
        }
    }
}
