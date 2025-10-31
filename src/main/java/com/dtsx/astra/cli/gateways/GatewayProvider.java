package com.dtsx.astra.cli.gateways;

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
import com.dtsx.astra.cli.gateways.pcu.PcuGateway;
import com.dtsx.astra.cli.gateways.pcu.associations.PcuAssociationsGateway;
import com.dtsx.astra.cli.gateways.role.RoleGateway;
import com.dtsx.astra.cli.gateways.streaming.StreamingGateway;
import com.dtsx.astra.cli.gateways.token.TokenGateway;
import com.dtsx.astra.cli.gateways.upgrade.UpgradeGateway;
import com.dtsx.astra.cli.gateways.user.UserGateway;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

public interface GatewayProvider {
    DbGateway mkDbGateway(AstraToken token, AstraEnvironment env, CompletionsCache dbCompletionsCache);

    PcuGateway mkPcuGateway(AstraToken token, AstraEnvironment env, CompletionsCache pcuCompletionsCache);

    PcuAssociationsGateway mkPcuAssociationsGateway(AstraToken token, AstraEnvironment env, PcuGateway pcuGateway);

    OrgGateway mkOrgGateway(AstraToken token, AstraEnvironment env);
    
    OrgGateway.Stateless mkOrgGatewayStateless();
    
    CollectionGateway mkCollectionGateway(AstraToken token, AstraEnvironment env);
    
    KeyspaceGateway mkKeyspaceGateway(AstraToken token, AstraEnvironment env);
    
    CdcGateway mkCdcGateway(AstraToken token, AstraEnvironment env);
    
    RegionGateway mkRegionGateway(AstraToken token, AstraEnvironment env);
    
    DownloadsGateway mkDownloadsGateway();
    
    StreamingGateway mkStreamingGateway(AstraToken token, AstraEnvironment env, CompletionsCache tenantCompletionsCache);
    
    RoleGateway mkRoleGateway(AstraToken token, AstraEnvironment env, CompletionsCache roleCompletionsCache);
    
    TableGateway mkTableGateway(AstraToken token, AstraEnvironment env);
    
    TokenGateway mkTokenGateway(AstraToken token, AstraEnvironment env);

    UserGateway mkUserGateway(AstraToken token, AstraEnvironment env, CompletionsCache userCompletionsCache);

    UpgradeGateway mkUpgradeGateway();
}
