package com.dtsx.astra.cli.gateways.db.cdc;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.CdcRef;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.core.models.TableRef;
import com.dtsx.astra.cli.core.models.TenantName;
import com.dtsx.astra.cli.gateways.SomeGateway;
import com.dtsx.astra.sdk.streaming.domain.CdcDefinition;

import java.util.stream.Stream;

public interface CdcGateway extends SomeGateway {
    Stream<CdcDefinition> findAll(DbRef dbRef);

    CreationStatus<Void> create(TableRef tableRef, TenantName tenantName, int topicPartition);

    DeletionStatus<Void> delete(CdcRef cdcRef);
}
