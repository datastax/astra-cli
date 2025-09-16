package com.dtsx.astra.cli.gateways;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.completions.CompletionsCache;
import com.dtsx.astra.cli.core.models.AstraToken;
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

public interface GatewayProvider {
    DbGateway mkDbGateway(AstraToken token, AstraEnvironment env, CompletionsCache dbCompletionsCache, CliContext ctx);
    
    OrgGateway mkOrgGateway(AstraToken token, AstraEnvironment env, CliContext ctx);
    
    OrgGateway.Stateless mkOrgGatewayStateless(CliContext ctx);
    
    CollectionGateway mkCollectionGateway(AstraToken token, AstraEnvironment env, CliContext ctx);
    
    KeyspaceGateway mkKeyspaceGateway(AstraToken token, AstraEnvironment env, CliContext ctx);
    
    CdcGateway mkCdcGateway(AstraToken token, AstraEnvironment env, CliContext ctx);
    
    RegionGateway mkRegionGateway(AstraToken token, AstraEnvironment env, CliContext ctx);
    
    DownloadsGateway mkDownloadsGateway(CliContext ctx);
    
    StreamingGateway mkStreamingGateway(AstraToken token, AstraEnvironment env, CompletionsCache tenantCompletionsCache, CliContext ctx);
    
    RoleGateway mkRoleGateway(AstraToken token, AstraEnvironment env, CompletionsCache roleCompletionsCache, CliContext ctx);
    
    TableGateway mkTableGateway(AstraToken token, AstraEnvironment env, CliContext ctx);
    
    TokenGateway mkTokenGateway(AstraToken token, AstraEnvironment env, CliContext ctx);
    
    UserGateway mkUserGateway(AstraToken token, AstraEnvironment env, CompletionsCache userCompletionsCache, CliContext ctx);
}
