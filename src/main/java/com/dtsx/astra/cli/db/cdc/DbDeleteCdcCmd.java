package com.dtsx.astra.cli.db.cdc;

import com.dtsx.astra.cli.core.out.LoggerShell;
import com.dtsx.astra.cli.db.AbstractDatabaseCmd;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Delete a DB is exist.
 */
@Command(name = "delete-cdc", description = "Delete a CDC connection")
public class DbDeleteCdcCmd extends AbstractDatabaseCmd {

    /**
     * Cdc Identifier
     */
    @Option(name = { "-id" }, title = "CDC_ID", arity = 1,
            description = "Identifier of the cdc when providing")
    protected String cdc;

    /**
     * keyspace.
     */
    @Option(name = { "-k", "--keyspace" }, title = "KEYSPACE", arity = 1, description = "Keyspace name")
    protected String keyspace;

    /**
     * table name.
     */
    @Option(name = { "--table" }, title = "TABLE", arity = 1, description = "Table name")
    protected String table;

    /**
     * tenant name.
     */
    @Option(name = { "--tenant" }, title = "TENANT", arity = 1, description = "Tenant name")
    protected String tenant;

    /** {@inheritDoc} */
    public void execute() {
        if (cdc != null && !cdc.equals("")) {
            LoggerShell.info("Deleting cdc with id:'%s'".formatted(cdc));
            ServiceCdc.getInstance().deleteCdcById(db, cdc);
        } else {
            LoggerShell.info("Deleting cdc from its full definition keyspace/table/tenant");
            ServiceCdc.getInstance().deleteCdcByDefinition(db, keyspace, table, tenant);
        }

    }
    
}
