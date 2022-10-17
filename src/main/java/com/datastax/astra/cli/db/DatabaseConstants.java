package com.datastax.astra.cli.db;

/**
 * Constants in the DB world.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public interface DatabaseConstants {
    
    /** Command constants. */
    String DB                    = "db";
    
    /** Default region. **/
    String DEFAULT_REGION        = "us-east-1";
    
    /** Default tier. **/
    String DEFAULT_TIER          = "serverless";
    
    /** Allow Snake case. */
    String KEYSPACE_NAME_PATTERN = "^[_a-z0-9]+$";
    
    /** column names. */
    String COLUMN_ID                = "id";
    /** column names. */
    String COLUMN_NAME              = "Name";
    /** column names. */
    String COLUMN_DEFAULT_REGION    = "Default Region";
    /** column names. */
    String COLUMN_REGIONS           = "Regions";
    /** column names. */
    String COLUMN_DEFAULT_CLOUD     = "Default Cloud Provider";
    /** column names. */
    String COLUMN_STATUS            = "Status";
    /** column names. */
    String COLUMN_DEFAULT_KEYSPACE  = "Default Keyspace";
    /** column names. */
    String COLUMN_KEYSPACES         = "Keyspaces";
    /** column names. */
    String COLUMN_CREATION_TIME     = "Creation Time";

}
