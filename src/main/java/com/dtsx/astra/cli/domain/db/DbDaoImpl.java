package com.dtsx.astra.cli.domain.db;

import com.dtsx.astra.cli.domain.APIProviderImpl;
import com.dtsx.astra.cli.exceptions.db.CouldNotResumeDbException;
import com.dtsx.astra.cli.exceptions.db.DbNotFoundException;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseCreationRequest;
import com.dtsx.astra.sdk.db.domain.DatabaseFilter;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class DbDaoImpl implements DbDao {
    private final APIProviderImpl api;
    private final String token;
    private final DbCache dbCache;

    public DbDaoImpl(String token, AstraEnvironment env, DbCache dbCache) {
        this.api = new APIProviderImpl(token, env, dbCache);
        this.token = token;
        this.dbCache = dbCache;
    }

    @Override
    public List<Database> findAll() {
        return api.dbOpsClient().search(DatabaseFilter.builder()
            .limit(1000)
            .build()).toList();
    }

    @Override
    public Database findOne(DbRef ref) {
        return api.tryResolveDb(ref).orElseThrow(() -> new DbNotFoundException(ref));
    }

    @SneakyThrows
    @Override
    public void resume(DbRef ref) {
        try {
            val endpoint = api.restApiEndpoint(ref) + "/v2/schemas/keyspace";

            @Cleanup val client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(20))
                .build();

            val request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(20))
                .header("Content-Type", "application/json")
                .header("X-Cassandra-Token", token)
                .GET()
                .build();

            val response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 500) {
                throw new CouldNotResumeDbException(ref, response.body());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public UUID create(String name, String keyspace, String region, CloudProviderType cloud, String tier, int capacityUnits, boolean vector) {
        val builder = DatabaseCreationRequest.builder()
            .name(name)
            .tier(tier)
            .capacityUnit(capacityUnits)
            .cloudProvider(cloud)
            .cloudRegion(region)
            .keyspace(keyspace);

        if (vector) {
            builder.withVector();
        }

        val id = UUID.fromString(
            api.dbOpsClient().create(builder.build())
        );

        dbCache.cacheDbId(name, id);
        dbCache.cacheDbRegion(id, region);
        return id;
    }
}
