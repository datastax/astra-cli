package com.dtsx.astra.cli.gateways.org;

import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.db.domain.RegionType;
import com.dtsx.astra.sdk.org.domain.Organization;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.SortedMap;
import java.util.TreeMap;

@RequiredArgsConstructor
public class OrgGatewayImpl implements OrgGateway {
    private final APIProvider apiProvider;

    @Override
    public Organization getCurrentOrg() {
        return AstraLogger.loading("Fetching current organization", (_) -> {
            return apiProvider.astraOpsClient().getOrganization();
        });
    }
}
