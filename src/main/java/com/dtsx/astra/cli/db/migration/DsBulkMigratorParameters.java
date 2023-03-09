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
 * Build DSBulk Migrator command based on the parameters
 * provided in Astra CLI.
 */
public interface DsBulkMigratorParameters {

    // ---- EXPORT ----

    /**
     * Produce CQL scripts optimized for DataStax Astra. Astra does not allow
     * some options in DDL statements; using this option, forbidden options
     * will be omitted from the generated CQL files.
     */
    String OPTION_OPTIMIZE_FOR_ASTRA = "-a";

    /**
     * The maximum number of concurrent files to write to. Must be a positive
     * number or the special value AUTO. The default is AUTO.
     */
    String OPTION_EXPORT_MAX_CONCURRENT_FILES = "--export-max-concurrent-files";

    /**
     * The maximum number of concurrent queries to execute. Must be a
     * positive number or the special value AUTO. The default is AUTO.
     */
    String OPTION_EXPORT_MAX_CONCURRENT_QUERIES = "--export-max-concurrent-queries";

    /**
     * The maximum number of records to export for each table. Must be a
     * positive number or -1. The default is -1 (export the entire table).
     */
    String OPTION_EXPORT_MAX_RECORDS = "--export-max-records";

    /**
     * The maximum number of token range queries to generate. Use the NC
     * syntax to specify a multiple of the number of available cores, e.g.
     * 8C = 8 times the number of available cores. The default is 8C. This
     * is an advanced setting; you should rarely need to modify the default
     */
    String OPTION_EXPORT_SPLITS ="--export-splits";

    /**
     * The path to a secure connect bundle to connect to the origin cluster
     * - (if that cluster is a DataStax Astra cluster)
     */
    String OPTION_EXPORT_BUNDLE = "--export-bundle";

    /**
     * The host name or IP and, optionally, the port of a node from the
     *  origin cluster. If the port is not specified, it will default to
     *  9042. This option can be specified multiple times.
     */
    String OPTION_EXPORT_HOST = "--export-host";

    /**
     * The password to use to authenticate against the origin cluster.
     */
    String OPTION_EXPORT_PASSWORD = "--export-password";

    /**
     * The username to use to authenticate against the origin cluster.
     */
    String OPTION_EXPORT_USERNAME = "--export-username";

    /**
     * An extra DSBulk option to use when exporting
     */
    String OPTION_EXPORT_DSBULK_OPTION = "--export-dsbulk-option";

    /**
     * The consistency level to use when exporting data.
     * The default is LOCAL_QUORUM.
     */
    String OPTION_EXPORT_CONSISTENCY = "--export-consistency";

    // ---- IMPORT ----

    /**
     * The path to a secure connect bundle to connect to the destination cluster
     * - (if that cluster is a DataStax Astra cluster)
     */
    String OPTION_IMPORT_BUNDLE = "--import-bundle";

    /**
     * The consistency level to use when writing the data.
     * The default is LOCAL_QUORUM.
     */
    String OPTION_IMPORT_CONSISTENCY = "--import-consistency";

    /**
     * The default timestamp to use when importing data. Must be a valid
     * instant in ISO-8601 syntax. The default is 1970-01-01T00:00:00Z.
     */
    String OPTION_DEFAULT_TIMESTAMP = "--import-default-timestamp";

    /**
     *  An extra DSBulk option to use when importing. Any valid DSBulk option
     *  can be specified here, and it will passed as is to the DSBulk
     *  process. DSBulk options, including driver options, must be passed as
     *  '--long.option.name=<value>'. Short options are not supported.
     */
    String OPTION_IMPORT_DSBULK_OPTION = "--import-dsbulk-option";

    /**
     * The host name or IP and, optionally, the port of a node from the
     * target cluster. If the port is not specified, it will default to
     * 9042. This option can be specified multiple times.
     */
    String OPTION_IMPORT_HOST="--import-host";

    /**
     * The maximum number of concurrent files to read from. Must be a
     * positive number or the special value AUTO. The default is AUTO.
     */
    String OPTION_IMPORT_MAX_CONCURRENT_FILES="--import-max-concurrent-files";

    /**
     * The maximum number of concurrent queries to execute. Must be a
     * positive number or the special value AUTO. The default is AUTO.
     */
    String OPTION_IMPORT_MAX_CONCURRENT_QUERIES="--import-max-concurrent-queries";

    /**
     * The maximum number of failed records to tolerate when importing data.
     * The default is 1000. Failed records will appear in a load.bad file
     */
    String OPTION_IMPORT_MAX_ERRORS= "--import-max-errors";

    /**
     * The password to use to authenticate against the target cluster.
     */
    String OPTION_IMPORT_PASSWORD = "--import-password";

    /**
     * The username to use to authenticate against the target cluster.
     */
    String OPTION_IMPORT_USERNAME = "--import-username";


    // ---- SETTING ----

    /**
     * The directory where CQL files will be generated. The default is a
     * 'data' subdirectory in the current working directory. The data
     * directory will be created if it does not exist.
     */
    String OPTION_DATA_DIRECTORY = "-d";

    /**
     * The username to use to authenticate against the origin cluster.
     */
    String OPTION_KEYSPACES = "--keyspaces";

    /**
     *  A regular expression to select tables to export.The default is to
     *  export all tables inside the keyspaces that were selected for export
     */
    String OPTION_TABLES = "--tables";

    /**
     * The table types to migrate (regular, counter, or all). Default is all.
     */
    String OPTION_TABLE_TYPES = "--table-types";

    /**
     * The directory where DSBulk should store its logs. The default is a
     * 'logs' subdirectory in the current working directory. This
     * subdirectory will be created if it does not exist. Each DSBulk
     * operation will create a subdirectory inside the log directory
     * specified here.
     */
    String OPTION_LOG_DIR="--dsbulk-log-dir";

    /**
     * Useful to provide the DSBulk location (from CLI)
     */
    String OPTION_DSBULK_CMD="--dsbulk-cmd";

    /**
     * Specialization of table types.
      */
    enum TableType {regular, counter, all};

}
