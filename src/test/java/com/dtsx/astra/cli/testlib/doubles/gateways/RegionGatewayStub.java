package com.dtsx.astra.cli.testlib.doubles.gateways;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.RegionName;
import com.dtsx.astra.cli.gateways.db.region.RegionGateway;
import com.dtsx.astra.sdk.db.domain.CloudProviderType;
import com.dtsx.astra.sdk.db.domain.Datacenter;

import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import static com.dtsx.astra.cli.testlib.doubles.Utils.methodIllegallyCalled;

public class RegionGatewayStub implements RegionGateway {
    @Override
    public SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>> findAllServerless(boolean vector) {
        return methodIllegallyCalled();
    }

    @Override
    public SortedMap<CloudProviderType, ? extends SortedMap<String, RegionInfo>> findAllClassic() {
        return methodIllegallyCalled();
    }

    @Override
    public List<Datacenter> findAllForDb(DbRef dbRef) {
        return methodIllegallyCalled();
    }

    @Override
    public Set<CloudProviderType> findAvailableClouds() {
        return methodIllegallyCalled();
    }

    @Override
    public CreationStatus<RegionName> create(DbRef ref, RegionName region, String tier, CloudProviderType cp) {
        return methodIllegallyCalled();
    }

    @Override
    public DeletionStatus<RegionName> delete(DbRef ref, RegionName region) {
        return methodIllegallyCalled();
    }
}
