package com.dtsx.astra.cli.testlib.doubles.gateways;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.CdcRef;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.gateways.db.cdc.CdcGateway;
import com.dtsx.astra.sdk.streaming.domain.CdcDefinition;

import java.util.stream.Stream;

import static com.dtsx.astra.cli.testlib.doubles.Utils.methodIllegallyCalled;

public class CdcGatewayStub extends GatewayStub implements CdcGateway {
    @Override
    public Stream<CdcDefinition> findAll(DbRef dbRef) {
        return methodIllegallyCalled();
    }

    @Override
    public CreationStatus<Void> create(TableRef tableRef, TenantName tenantName, int topicPartition) {
        return methodIllegallyCalled();
    }

    @Override
    public DeletionStatus<Void> delete(CdcRef cdcRef) {
        return methodIllegallyCalled();
    }
}
