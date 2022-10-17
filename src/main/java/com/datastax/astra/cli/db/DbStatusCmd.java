package com.datastax.astra.cli.db;

import com.github.rvesse.airline.annotations.Command;

/**
 * Display information relative to a db.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "status", description = "Show status of a database")
public class DbStatusCmd extends AbstractDatabaseCmd {

    /** {@inheritDoc}  */
    public void execute() {
        dbServices.showDbStatus(db);
    }

}
