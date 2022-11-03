package com.dtsx.astra.cli.streaming;

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

import com.dtsx.astra.cli.core.exception.InvalidArgumentException;
import com.dtsx.astra.cli.streaming.exception.TenantNotFoundException;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

import java.util.Arrays;

/**
 * Display information relative to a db.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "get", description = "Show details of a tenant")
public class StreamingGetCmd extends AbstractStreamingCmd {

    /** Enum for db get. */
    public enum StreamingGetKeys {
        /** tenant status */
        STATUS("status"),
        /** cloud provider*/
        CLOUD("cloud"),
        /** pulsar token */
        PULSAR_TOKEN("pulsar_token"),
        /** cloud region */
        REGION("region");

        /** hold code value. */
        private final String key;

        /**
         * Default constructor.
         *
         * @param key
         *      key value
         */
        StreamingGetKeys(String key) {
            this.key = key;
        }

        /**
         * Access key value.
         * @return
         *      key value
         */
        public String getKey() {
            return key;
        }

        /**
         * Create on from code.
         *
         * @param key
         *      key value.
         * @return
         *      instance of get key
         */
        public static StreamingGetKeys fromKey(String key) {
            return StreamingGetKeys.valueOf(key.toUpperCase());
        }
    }

    /** Authentication token used if not provided in config. */
    @Option(name = { "-k", "--key" }, title = "Key", description = ""
            + "Show value for a property among: "
            + "'status', 'cloud', 'pulsar_token', 'region'")
    protected String key;
    
    /** {@inheritDoc} */
    public void execute()
    throws TenantNotFoundException {
        StreamingGetKeys sKey = null;
        if (null != key) {
            try {
                sKey = StreamingGetKeys.fromKey(key);
            } catch (Exception e) {
                throw new InvalidArgumentException("Invalid key. Expected one of %s".formatted(
                        Arrays.stream(StreamingGetKeys.values())
                                .map(StreamingGetKeys::name)
                                .map(String::toLowerCase)
                                .toList()
                                .toString()));
            }
        }
        OperationsStreaming.showTenant(tenant, sKey);
    }

}
