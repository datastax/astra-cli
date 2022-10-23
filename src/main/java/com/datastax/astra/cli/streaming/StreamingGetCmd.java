package com.datastax.astra.cli.streaming;

import com.datastax.astra.cli.core.AbstractConnectedCmd;
import com.datastax.astra.cli.core.exception.InvalidArgumentException;
import com.datastax.astra.cli.streaming.exception.TenantNotFoundException;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;
import com.github.rvesse.airline.annotations.restrictions.Required;

import java.util.Arrays;

/**
 * Display information relative to a db.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "get", description = "Show details of a tenant")
public class StreamingGetCmd extends AbstractConnectedCmd {

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
    
    /** name of the DB. */
    @Required
    @Arguments(title = "TENANT", description = "Tenant name ")
    public String tenant;
    
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
