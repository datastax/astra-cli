package com.dtsx.astra.cli.streaming.cdc;

/*-
 * #%L
 * Astra Cli
 * %%
 * Copyright (C) 2022 DataStax
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.dtsx.astra.cli.streaming.AbstractStreamingCmd;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Declare a Change Data Capture between DB and Pulsar.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "create-cdc", description = "Create a CDC from a DB to Pulsar")
public class StreamingCreateCdcCmd extends AbstractStreamingCmd {

    /**  Options. */
    @Option(name = {"-db", "--database" }, title = "DATABASE", arity = 1,
            description = "Database name or identifier")
    protected String db;

    /** Options. */
    @Option(name = {"-k", "--keyspace" }, title = "KEYSPACE", arity = 1,
            description = "Authenticate to the given keyspace.")
    protected String keyspace;

    /**  Options. */
    @Option(name = {"-t", "--table" }, title = "TABLE", arity = 1,
            description = "Table name")
    protected String table;

    @Option(name = {"-p", "--partition" }, title = "topicPartition",
            description = "Partition in topic")
    protected int topicPartition = 3;

    /** {@inheritDoc} */
    public void execute() {
        throw new UnsupportedOperationException("Create Cdc function is under development");
    }

}
