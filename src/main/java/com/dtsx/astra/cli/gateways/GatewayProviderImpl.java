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
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.gateways.role.RoleGatewayCompletionsCacheWrapper;
import com.dtsx.astra.cli.gateways.role.RoleGatewayImpl;
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

public class GatewayProviderImpl implements GatewayProvider {
    @Override
    public DbGateway mkDbGateway(AstraToken token, AstraEnvironment env, CompletionsCache dbCompletionsCache, CliContext ctx) {
        return new DbGatewayCompletionsCacheWrapper(new DbGatewayImpl(ctx, APIProvider.mkDefault(token, env, ctx), token, env, GlobalInfoCache.INSTANCE, mkRegionGateway(token, env, ctx)), dbCompletionsCache);
    }

    @Override
    public OrgGateway mkOrgGateway(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return new OrgGatewayImpl(ctx, APIProvider.mkDefault(token, env, ctx));
    }

    @Override
    public OrgGateway.Stateless mkOrgGatewayStateless(CliContext ctx) {
        return new OrgGatewayImpl.StatelessImpl(ctx);
    }

    @Override
    public CollectionGateway mkCollectionGateway(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return new CollectionGatewayImpl(ctx, APIProvider.mkDefault(token, env, ctx));
    }

    @Override
    public KeyspaceGateway mkKeyspaceGateway(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return new KeyspaceGatewayImpl(ctx, APIProvider.mkDefault(token, env, ctx));
    }

    @Override
    public CdcGateway mkCdcGateway(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return new CdcGatewayImpl(ctx, APIProvider.mkDefault(token, env, ctx));
    }

    @Override
    public RegionGateway mkRegionGateway(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return new RegionGatewayImpl(ctx, APIProvider.mkDefault(token, env, ctx));
    }

    @Override
    public DownloadsGateway mkDownloadsGateway(CliContext ctx) {
        return new DownloadsGatewayImpl(ctx);
    }

    @Override
    public StreamingGateway mkStreamingGateway(AstraToken token, AstraEnvironment env, CompletionsCache tenantCompletionsCache, CliContext ctx) {
        return new StreamingGatewayCompletionsCacheWrapper(new StreamingGatewayImpl(ctx, APIProvider.mkDefault(token, env, ctx)), tenantCompletionsCache);
    }

    @Override
    public RoleGateway mkRoleGateway(AstraToken token, AstraEnvironment env, CompletionsCache roleCompletionsCache, CliContext ctx) {
        return new RoleGatewayCompletionsCacheWrapper(new RoleGatewayImpl(ctx, APIProvider.mkDefault(token, env, ctx)), roleCompletionsCache);
    }

    @Override
    public TableGateway mkTableGateway(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return new TableGatewayImpl(ctx, APIProvider.mkDefault(token, env, ctx));
    }

    @Override
    public TokenGateway mkTokenGateway(AstraToken token, AstraEnvironment env, CliContext ctx) {
        return new TokenGatewayImpl(ctx, APIProvider.mkDefault(token, env, ctx), mkRoleGateway(token, env, new RoleCompletionsCache(ctx), ctx), mkOrgGatewayStateless(ctx));
    }

    @Override
    public UserGateway mkUserGateway(AstraToken token, AstraEnvironment env, CompletionsCache userCompletionsCache, CliContext ctx) {
        return new UserGatewayCompletionsCacheWrapper(new UserGatewayImpl(ctx, APIProvider.mkDefault(token, env, ctx), new RoleGatewayImpl(ctx, APIProvider.mkDefault(token, env, ctx))), userCompletionsCache);
    }

    @Override
    public UpgradeGateway mkUpgradeGateway(CliContext ctx) {
        return new UpgradeGatewayImpl(ctx);
    }
}
