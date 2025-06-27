package com.dtsx.astra.cli.gateways.db.cdc;

import com.dtsx.astra.cli.core.datatypes.CreationStatus;
import com.dtsx.astra.cli.core.datatypes.DeletionStatus;
import com.dtsx.astra.cli.core.models.*;
import com.dtsx.astra.cli.gateways.APIProvider;
import com.dtsx.astra.sdk.streaming.domain.CdcDefinition;
import com.dtsx.astra.sdk.utils.AstraEnvironment;

import java.util.List;

public interface CdcGateway {
    static CdcGateway mkDefault(Token token, AstraEnvironment env) {
        return new CdcGatewayImpl(APIProvider.mkDefault(token, env));
    }

    List<CdcDefinition> findAll(DbRef dbRef);

    CreationStatus<Void> create(TableRef tableRef, TenantName tenantName, int topicPartition);

    DeletionStatus<Void> delete(CdcRef cdcRef);
}
