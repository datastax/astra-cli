package com.dtsx.astra.cli.gateways.db.cdc;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.*;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.streaming.domain.CdcDefinition;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.stream.Stream;

public interface CdcGateway {

    Stream<CdcDefinition> findAll(DbRef dbRef);

    CreationStatus<Void> create(TableRef tableRef, TenantName tenantName, int topicPartition);

    DeletionStatus<Void> delete(CdcRef cdcRef);
}
