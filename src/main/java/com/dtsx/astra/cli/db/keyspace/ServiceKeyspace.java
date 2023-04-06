package com.dtsx.astra.cli.db.keyspace;

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

import com.dtsx.astra.cli.core.exception.InvalidArgumentException;
import com.dtsx.astra.cli.core.out.AstraCliConsole;
import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.core.out.ShellTable;
import com.dtsx.astra.cli.db.DaoDatabase;
import com.dtsx.astra.cli.db.exception.DatabaseNameNotUniqueException;
import com.dtsx.astra.cli.db.exception.InvalidDatabaseStateException;
import com.dtsx.astra.sdk.db.domain.Database;
import com.dtsx.astra.sdk.db.domain.DatabaseStatusType;
import com.dtsx.astra.sdk.db.exception.DatabaseNotFoundException;
import com.dtsx.astra.sdk.db.exception.KeyspaceAlreadyExistException;
import com.dtsx.astra.sdk.db.exception.KeyspaceNotFoundException;

import java.util.HashMap;
import java.util.Map;

/**
 * Group services related to keyspaces.
 */
public class ServiceKeyspace {

    /** column names. */
    static final String COLUMN_NAME = "Name";

    /** working object. */
    static final String KS = "Keyspace ";

    /** Allow Snake case. */
    public static final String KEYSPACE_NAME_PATTERN = "^[_a-z0-9]+$";

    /**
     * Singleton Pattern
     */
    private static ServiceKeyspace instance;

    /**
     * Access to databases object.
     */
    private final DaoDatabase dbDao;

    /**
     * Singleton Pattern.
     *
     * @return
     *      instance of the service.
     */
    public static synchronized ServiceKeyspace getInstance() {
        if (null == instance) {
            instance = new ServiceKeyspace();
        }
        return instance;
    }

    /**
     * Default Constructor.
     */
    private ServiceKeyspace() {
        this.dbDao = DaoDatabase.getInstance();
    }

    /**
     * List keyspaces of a database.
     *
     * @param databaseName
     *      database name
     */
    public void listKeyspaces(String databaseName) {
        Database db  = dbDao.getDatabase(databaseName);
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_NAME,    20);
        db.getInfo().getKeyspaces().forEach(ks -> {
            Map<String, String> rf = new HashMap<>();
            if (db.getInfo().getKeyspace().equals(ks)) {
                rf.put(COLUMN_NAME, ks + " (default)");
            } else {
                rf.put(COLUMN_NAME, ks);
            }
            sht.getCellValues().add(rf);
        });
        AstraCliConsole.printShellTable(sht);
    }

    /**
     * Create a keyspace if not exist.
     *
     * @param ifNotExist
     *      flag to disable error if already exists
     * @param databaseName
     *      db name
     * @param keyspaceName
     *      ks name
     * @throws DatabaseNameNotUniqueException
     *      error if db name is not unique
     * @throws DatabaseNotFoundException
     *      error is db is not found
     * @throws InvalidArgumentException
     *      invalid parameter
     * @throws KeyspaceAlreadyExistException
     *      keyspace exist and --if-not-exist option not provided
     */
    public void createKeyspace(String databaseName, String keyspaceName, boolean ifNotExist)
            throws DatabaseNameNotUniqueException, DatabaseNotFoundException,
            InvalidArgumentException, KeyspaceAlreadyExistException {

        // Validate keyspace names
        if (!keyspaceName.matches(KEYSPACE_NAME_PATTERN))
            throw new InvalidArgumentException("The keyspace name is not valid, please use snake_case: [a-z0-9_]");

        if (dbDao.getDatabase(databaseName).getInfo().getKeyspaces().contains(keyspaceName)) {
            if (ifNotExist) {
                LoggerShell.info("%s '%s' already exists. Connecting to keyspace.".formatted(KS, keyspaceName));
            } else {
                throw new KeyspaceAlreadyExistException(keyspaceName, databaseName);
            }
        } else {
            try {
                dbDao.getRequiredDatabaseClient(databaseName).keyspaces().create(keyspaceName);
                LoggerShell.info("%s '%s' is creating.".formatted(KS, keyspaceName));
            } catch(Exception e) {
                throw new InvalidDatabaseStateException(databaseName, DatabaseStatusType.ACTIVE,
                        dbDao.getDatabase(databaseName).getStatus());
            }
        }
    }


    /**
     * Delete a keyspace if exists.
     *
     * @param databaseName
     *      db name
     * @param keyspaceName
     *      ks name
     * @throws DatabaseNameNotUniqueException
     *      error if db name is not unique
     * @throws DatabaseNotFoundException
     *      error is db is not found
     * @throws InvalidArgumentException
     *      invalid parameter
     * @throws KeyspaceNotFoundException
     *      keyspace does not exist
     */
    public void deleteKeyspace(String databaseName, String keyspaceName)
    throws DatabaseNameNotUniqueException, DatabaseNotFoundException,
            InvalidArgumentException, KeyspaceNotFoundException {
        dbDao.getRequiredDatabaseClient(databaseName).keyspaces().delete(keyspaceName);
    }

}
