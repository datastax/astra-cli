package com.dtsx.astra.cli.services.db;

import com.dtsx.astra.cli.output.AstraLogger;
import com.dtsx.astra.cli.services.APIProvider;
import com.dtsx.astra.cli.services.DbService;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseFilter;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;

import java.util.List;

public class DbServiceImpl implements DbService {
    private final APIProvider apiProvider;
    private final DatabaseDao databaseDao;

    public DbServiceImpl(String token, AstraEnvironment env) {
        this.apiProvider = APIProvider.mkDefault("default", token, env);
        this.databaseDao = new DatabaseDao(apiProvider);
    }

    @Override
    public void waitUntilDbActive(String dbName, int timeout) {
        val dbClient = databaseDao.getDatabaseClient(dbName);

        if (dbClient.isEmpty()) {
            throw new DatabaseNotFoundException(dbName);
        }

        val optDb = dbClient.get().find();

        if (optDb.isPresent()) {
            val db = optDb.get();

            if (db.getStatus().equals(DatabaseStatusType.ACTIVE)) {
                return;
            }

            retryUntilTimeoutOrSuccess(db, timeout);
        }
    }

    @Override
    public List<Database> findDatabases() {
        return AstraLogger.loading("Fetching databases...", null, (_) -> (
            apiProvider.devopsApiClient().db()
                .search(DatabaseFilter.builder().limit(1000).build())
                .toList()
        ));
    }

    @Override
    public Database getDbInfo(String dbName) {
        return AstraLogger.loading("Fetching database info for '%s'...".formatted(dbName), null, (_) -> (
            databaseDao.getDatabase(dbName)
        ));
    }

    private void retryUntilTimeoutOrSuccess(Database db, int timeout) {
        int retries = 0;

        while (((timeout == 0) || (retries++ < timeout)) && !db.getStatus().equals(DatabaseStatusType.ACTIVE)) {
            try {
                Thread.sleep(1000);
                db = databaseDao.getDatabase(db.getInfo().getName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

    }
}
