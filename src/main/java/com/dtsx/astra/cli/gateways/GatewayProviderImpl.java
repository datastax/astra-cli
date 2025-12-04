package com.dtsx.astra.cli.gateways;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.CompletionsCache;
import com.dtsx.astra.cli.core.completions.caches.RoleCompletionsCache;
import com.dtsx.astra.cli.core.models.AstraToken;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.gateways.db.DbGatewayCompletionsCacheWrapper;
import com.dtsx.astra.cli.gateways.db.DbGatewayImpl;
import com.dtsx.astra.cli.gateways.db.cdc.CdcGateway;
import com.dtsx.astra.cli.gateways.db.cdc.CdcGatewayImpl;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGateway;
import com.dtsx.astra.cli.gateways.db.collection.CollectionGatewayImpl;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGateway;
import com.dtsx.astra.cli.gateways.db.keyspace.KeyspaceGatewayImpl;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.cli.gateways.db.region.RegionGatewayImpl;
import com.dtsx.astra.cli.gateways.db.table.TableGateway;
import com.dtsx.astra.cli.gateways.db.table.TableGatewayImpl;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGateway;
import com.dtsx.astra.cli.gateways.downloads.DownloadsGatewayImpl;
import com.dtsx.astra.cli.gateways.org.OrgGateway;
import com.dtsx.astra.cli.gateways.org.OrgGatewayImpl;
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.cli.gateways.pcu.PcuGatewayCompletionsCacheWrapper;
import com.dtsx.astra.cli.gateways.pcu.PcuGatewayImpl;
import com.dtsx.astra.cli.gateways.pcu.associations.PcuAssociationsGateway;
import com.dtsx.astra.cli.gateways.pcu.associations.PcuAssociationsGatewayImpl;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.gateways.role.RoleGatewayCompletionsCacheWrapper;
import com.dtsx.astra.cli.gateways.role.RoleGatewayImpl;
import com.dtsx.astra.cli.gateways.role.RoleGatewayPersistentCacheWrapper;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.gateways.streaming.StreamingGatewayCompletionsCacheWrapper;
import com.dtsx.astra.cli.gateways.streaming.StreamingGatewayImpl;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.gateways.token.TokenGatewayImpl;
import com.dtsx.astra.cli.gateways.upgrade.UpgradeGateway;
import com.dtsx.astra.cli.gateways.upgrade.UpgradeGatewayImpl;
import com.dtsx.astra.cli.gateways.user.UserGateway;
import com.dtsx.astra.cli.gateways.user.UserGatewayCompletionsCacheWrapper;
import com.dtsx.astra.cli.gateways.user.UserGatewayImpl;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.RequiredArgsConstructor;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class GatewayProviderImpl implements GatewayProvider {
    private final Supplier<CliContext> ctxSupplier;

    @Override
    public DbGateway mkDbGateway(AstraToken token, AstraEnvironment env, CompletionsCache dbCompletionsCache) {
        return new DbGatewayCompletionsCacheWrapper(new DbGatewayImpl(ctx(), apiProvider(token, env), token, env, GlobalInfoCache.INSTANCE, mkRegionGateway(token, env)), dbCompletionsCache);
    }

    @Override
    public PcuGateway mkPcuGateway(AstraToken token, AstraEnvironment env, CompletionsCache pcuCompletionsCache) {
        return new PcuGatewayCompletionsCacheWrapper(new PcuGatewayImpl(ctx(), apiProvider(token, env)), pcuCompletionsCache);
    }

    @Override
    public PcuAssociationsGateway mkPcuAssociationsGateway(AstraToken token, AstraEnvironment env) {
        return new PcuAssociationsGatewayImpl(ctx(), apiProvider(token, env));
    }

    @Override
    public OrgGateway mkOrgGateway(AstraToken token, AstraEnvironment env) {
        return new OrgGatewayImpl(ctx(), apiProvider(token, env));
    }

    @Override
    public OrgGateway.Stateless mkOrgGatewayStateless() {
        return new OrgGatewayImpl.StatelessImpl(ctx());
    }

    @Override
    public CollectionGateway mkCollectionGateway(AstraToken token, AstraEnvironment env) {
        return new CollectionGatewayImpl(ctx(), apiProvider(token, env));
    }

    @Override
    public KeyspaceGateway mkKeyspaceGateway(AstraToken token, AstraEnvironment env) {
        return new KeyspaceGatewayImpl(ctx(), apiProvider(token, env));
    }

    @Override
    public CdcGateway mkCdcGateway(AstraToken token, AstraEnvironment env) {
        return new CdcGatewayImpl(ctx(), apiProvider(token, env));
    }

    @Override
    public RegionGateway mkRegionGateway(AstraToken token, AstraEnvironment env) {
        return new RegionGatewayImpl(ctx(), apiProvider(token, env));
    }

    @Override
    public DownloadsGateway mkDownloadsGateway() {
        return new DownloadsGatewayImpl(ctx());
    }

    @Override
    public StreamingGateway mkStreamingGateway(AstraToken token, AstraEnvironment env, CompletionsCache tenantCompletionsCache) {
        return new StreamingGatewayCompletionsCacheWrapper(new StreamingGatewayImpl(ctx(), apiProvider(token, env)), tenantCompletionsCache);
    }

    @Override
    public RoleGateway mkRoleGateway(AstraToken token, AstraEnvironment env, CompletionsCache roleCompletionsCache) {
        return new RoleGatewayPersistentCacheWrapper(ctx(), new RoleGatewayCompletionsCacheWrapper(new RoleGatewayImpl(ctx(), apiProvider(token, env)), roleCompletionsCache), ctx().home().dirs.cache::use);
    }

    @Override
    public TableGateway mkTableGateway(AstraToken token, AstraEnvironment env) {
        return new TableGatewayImpl(ctx(), apiProvider(token, env));
    }

    @Override
    public TokenGateway mkTokenGateway(AstraToken token, AstraEnvironment env) {
        return new TokenGatewayImpl(ctx(), apiProvider(token, env), mkRoleGateway(token, env, new RoleCompletionsCache(ctx())), mkOrgGatewayStateless());
    }

    @Override
    public UserGateway mkUserGateway(AstraToken token, AstraEnvironment env, CompletionsCache userCompletionsCache) {
        return new UserGatewayCompletionsCacheWrapper(new UserGatewayImpl(ctx(), apiProvider(token, env), new RoleGatewayImpl(ctx(), apiProvider(token, env))), userCompletionsCache);
    }

    @Override
    public UpgradeGateway mkUpgradeGateway() {
        return new UpgradeGatewayImpl(ctx());
    }

    private APIProvider apiProvider(AstraToken token, AstraEnvironment env) {
        return APIProvider.mkDefault(ctx(), token, env);
    }

    private CliContext ctx() {
        return ctxSupplier.get();
    }
}
