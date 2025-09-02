package com.dtsx.astra.cli.testlib.doubles;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.CompletionsCache;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.GatewayProvider;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.cdc.CdcGateway;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.gateways.user.UserGateway;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.val;

import java.util.*;
import java.util.stream.Stream;

@NoArgsConstructor
@AllArgsConstructor
public class GatewayProviderMock implements GatewayProvider {
    private Map<Class<?>, SomeGateway> instances = Map.of();

    private static final List<Class<?>> CLASSES = List.of(
        DbGateway.class,
        OrgGateway.class,
        OrgGateway.Stateless.class,
        CollectionGateway.class,
        KeyspaceGateway.class,
        CdcGateway.class,
        RegionGateway.class,
        DownloadsGateway.class,
        StreamingGateway.class,
        RoleGateway.class,
        TableGateway.class,
        TokenGateway.class,
        UserGateway.class
    );

    public DbGateway dbGateway() {
        return returnIfEnabled(DbGateway.class);
    }

    public OrgGateway orgGateway() {
        return returnIfEnabled(OrgGateway.class);
    }

    public OrgGateway.Stateless orgGatewayStateless() {
        return returnIfEnabled(OrgGateway.Stateless.class);
    }

    public CollectionGateway collectionGateway() {
        return returnIfEnabled(CollectionGateway.class);
    }

    public KeyspaceGateway keyspaceGateway() {
        return returnIfEnabled(KeyspaceGateway.class);
    }

    public CdcGateway cdcGateway() {
        return returnIfEnabled(CdcGateway.class);
    }

    public RegionGateway regionGateway() {
        return returnIfEnabled(RegionGateway.class);
    }

    public DownloadsGateway downloadsGateway() {
        return returnIfEnabled(DownloadsGateway.class);
    }

    public StreamingGateway streamingGateway() {
        return returnIfEnabled(StreamingGateway.class);
    }

    public RoleGateway roleGateway() {
        return returnIfEnabled(RoleGateway.class);
    }

    public TableGateway tableGateway() {
        return returnIfEnabled(TableGateway.class);
    }

    public TokenGateway tokenGateway() {
        return returnIfEnabled(TokenGateway.class);
    }

    public UserGateway userGateway() {
        return returnIfEnabled(UserGateway.class);
    }

    @Override
    public DbGateway mkDbGateway(AstraToken token, AstraEnvironment env, CompletionsCache dbCompletionsCache, CliContext ctx) {
        return dbGateway();
    }

    @Override
    public OrgGateway mkOrgGateway(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return orgGateway();
    }

    @Override
    public OrgGateway.Stateless mkOrgGatewayStateless(CliContext ctx) {
        return orgGatewayStateless();
    }

    @Override
    public CollectionGateway mkCollectionGateway(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return collectionGateway();
    }

    @Override
    public KeyspaceGateway mkKeyspaceGateway(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return keyspaceGateway();
    }

    @Override
    public CdcGateway mkCdcGateway(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return cdcGateway();
    }

    @Override
    public RegionGateway mkRegionGateway(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return regionGateway();
    }

    @Override
    public DownloadsGateway mkDownloadsGateway(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return downloadsGateway();
    }

    @Override
    public StreamingGateway mkStreamingGateway(AstraToken token, AstraEnvironment env, CompletionsCache tenantCompletionsCache, CliContext ctx) {
        return streamingGateway();
    }

    @Override
    public RoleGateway mkRoleGateway(AstraToken token, AstraEnvironment env, CompletionsCache roleCompletionsCache, CliContext ctx) {
        return roleGateway();
    }

    @Override
    public TableGateway mkTableGateway(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return tableGateway();
    }

    @Override
    public TokenGateway mkTokenGateway(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return tokenGateway();
    }

    @Override
    public UserGateway mkUserGateway(AstraToken token, AstraEnvironment env, CompletionsCache userCompletionsCache, CliContext ctx) {
        return userGateway();
    }

    public GatewayProviderMock withInstance(SomeGateway instance) {
        val newInstances = new HashMap<>(instances);

        val allGatewayInterfaces = recursivelyFindAllInterfaces(instance.getClass())
            .filter(CLASSES::contains)
            .toList();

        for (val clazz : allGatewayInterfaces) {
            newInstances.put(clazz, instance);
        }

        return new GatewayProviderMock(newInstances);
    }

    @SuppressWarnings("unchecked")
    private <T> T returnIfEnabled(Class<?> clazz) {
        return (T) Optional.ofNullable(instances.get(clazz)).orElseThrow(() -> new IllegalStateException(clazz.getSimpleName() + " was not enabled in the test"));
    }

    private Stream<Class<?>> recursivelyFindAllInterfaces(Class<?> clazz) {
        val interfaces = List.of(clazz.getInterfaces());

        return Stream.concat(
            interfaces.stream(),
            Stream.concat(
                interfaces.stream().flatMap(this::recursivelyFindAllInterfaces),
                Optional.ofNullable(clazz.getSuperclass()).stream().flatMap(this::recursivelyFindAllInterfaces)
            )
        );
    }
}
