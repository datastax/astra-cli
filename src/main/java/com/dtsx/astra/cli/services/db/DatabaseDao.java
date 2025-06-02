package com.dtsx.astra.cli.services.db;

/*-
 * #%L
 * Astra CLI
 * --
 * Copyright (C) 2022 - 2023 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.client.core.options.DataAPIClientOptions;
import com.datastax.astra.client.databases.DatabaseOptions;
import com.dtsx.astra.cli.exceptions.db.DatabaseNameNotUniqueException;
import com.dtsx.astra.cli.services.APIProvider;
import com.dtsx.astra.sdk.db.DbOpsClient;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.val;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class DatabaseDao {
    private final APIProvider apiProvider;

    public Database getDatabase(String dbName) {
        return getRequiredDatabaseClient(dbName)
            .find()
            .orElseThrow(() -> new DatabaseNotFoundException(dbName));
    }

    public com.datastax.astra.client.databases.Database getDataAPIDatabase(String dbName) {
        return getDataAPIDatabase(dbName, DataAPIClientOptions.DEFAULT_KEYSPACE);
    }

    public com.datastax.astra.client.databases.Database getDataAPIDatabase(String dbName, String keyspace) {
        val db = getDatabase(dbName);

        if (db.getInfo().getDbType() == null) {
            throw new IllegalArgumentException("Database %s is not a vector database".formatted(dbName));
        }

        return apiProvider.dataApiClient().getDatabase(UUID.fromString(db.getId()), new DatabaseOptions().keyspace(keyspace));
    }

    public DbOpsClient getRequiredDatabaseClient(String databaseName) {
        return getDatabaseClient(databaseName).orElseThrow(() -> new DatabaseNotFoundException(databaseName));
    }

    public Optional<DbOpsClient> getDatabaseClient(String dbName) throws DatabaseNameNotUniqueException {
        val dbsClient = apiProvider.devopsApiClient().db();

        dbName = dbName.replace("\"", "");

        if (!dbName.contains(" ") ) {
            val dbClient = dbsClient.database(dbName);

            if (dbClient.exist()) {
                return Optional.of(dbClient);
            }
        }

        val dbs = dbsClient.findByName(dbName).toList();

        if (dbs.size() > 1) {
            throw new DatabaseNameNotUniqueException(dbName);
        }

        if (1 == dbs.size()) {
            return Optional.ofNullable(dbsClient.database(dbs.getFirst().getId()));
        }
        return Optional.empty();
    }

//    @SneakyThrows
//    public void downloadCloudSecureBundles(String databaseName) {
//        getRequiredDatabaseClient(databaseName)
//            .downloadAllSecureConnectBundles(
//                    AstraCliUtils.ASTRA_HOME + File.separator + AstraCliUtils.SCB_FOLDER);
//    }

//    public void downloadCloudSecureBundle(String databaseName, String region, String location) {
//        Database        db  = getDatabase(databaseName);
//        Set<Datacenter> dcs = db.getInfo().getDatacenters();
//        Datacenter      dc  = dcs.iterator().next();
//        if (dcs.size() > 1) {
//            if (null == region) {
//                throw new IllegalArgumentException(
//                         "Your database is deployed on multiple regions. "
//                        + "A scb is associated to only one region. "
//                        + "Add -r or --region in the command to select one.");
//            }
//
//            Optional<Datacenter> optDc = dcs.stream()
//               .filter(d -> d.getName().equals(region)).findFirst();
//
//            if (optDc.isEmpty()) {
//                throw new IllegalArgumentException(
//                        "Your database is deployed on multiple regions. "
//                        + "You select and invalid region name. "
//                        + "Please use one from %s".formatted(
//                                dcs.stream().map(Datacenter::getName)
//                                   .toList().toString()));
//            }
//            dc = optDc.get();
//        }
//
//        // Default location
//        if (location == null) {
//            location = "." + File.separator + "scb_" + db.getId() + "_" + dc.getName() + ".zip";
//        }
//        FileUtils.downloadFile(dc.getSecureBundleUrl(), location);
//    }
}
