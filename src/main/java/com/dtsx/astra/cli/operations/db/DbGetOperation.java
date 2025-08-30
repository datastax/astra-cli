package com.dtsx.astra.cli.operations.db;

import com.dtsx.astra.cli.commands.db.DbGetCmd.DbGetKeys;
import com.dtsx.astra.cli.core.models.DbRef;
import com.dtsx.astra.cli.gateways.db.DbGateway;
import com.dtsx.astra.cli.operations.Operation;
import com.dtsx.astra.cli.operations.db.DbGetOperation.DbInfo;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.Datacenter;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;

@RequiredArgsConstructor
public class DbGetOperation implements Operation<DbInfo> {
    private final DbGateway dbGateway;
    private final DbGetRequest request;

    public record DbGetRequest(
        DbRef dbRef,
        Optional<DbGetKeys> key
    ) {}

    public sealed interface DbInfo {}

    public record DbInfoFull(
        Database database
    ) implements DbInfo {}

    public record DbInfoValue(
        Object value
    ) implements DbInfo {}

    @Override
    public DbInfo execute() {
        val database = dbGateway.findOne(request.dbRef);

        return request.key
            .map(key -> mkDbInfoValue(key, database))
            .orElseGet(() -> mkDbInfoFull(database));
    }

    private DbInfo mkDbInfoFull(Database database) {
        return new DbInfoFull(database);
    }

    private DbInfo mkDbInfoValue(DbGetKeys key, Database database) {
        val value = switch (key) {
            case name -> database.getInfo().getName();
            case id -> database.getId();
            case status -> database.getStatus();
            case cloud -> database.getInfo().getCloudProvider();
            case keyspace -> database.getInfo().getKeyspace();
            case keyspaces -> database.getInfo().getKeyspaces().stream().toList();
            case region -> database.getInfo().getRegion();
            case regions -> database.getInfo().getDatacenters().stream().map(Datacenter::getRegion).toList();
            case creation_time -> database.getCreationTime();
            case vector -> database.getInfo().getDbType().equals("vector");
        };

        return new DbInfoValue(value);
    }
}
