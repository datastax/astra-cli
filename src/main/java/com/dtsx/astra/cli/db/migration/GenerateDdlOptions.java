package com.dtsx.astra.cli.db.migration;

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

/**
 * Options Listing for generate-ddl
 */
public class GenerateDdlOptions {

    private String db;
    private String dataDir;

    private String keyspaces;

    private String tables;

    /**
     * Full fledge constructor.
     *
     * @param db
     *      database
     * @param dataDir
     *      data director
     * @param keyspaces
     *      keyspace expression
     * @param tables
     *      table expession
     */
    public GenerateDdlOptions(String db, String dataDir, String keyspaces, String tables) {
        this.db = db;
        this.dataDir = dataDir;
        this.keyspaces = keyspaces;
        this.tables = tables;
    }

    /**
     * Gets db
     *
     * @return value of db
     */
    public String getDb() {
        return db;
    }

    /**
     * Gets dataDir
     *
     * @return value of dataDir
     */
    public String getDataDir() {
        return dataDir;
    }

    /**
     * Gets keyspaces
     *
     * @return value of keyspaces
     */
    public String getKeyspaces() {
        return keyspaces;
    }

    /**
     * Gets tables
     *
     * @return value of tables
     */
    public String getTables() {
        return tables;
    }
}
