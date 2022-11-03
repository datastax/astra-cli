package com.dtsx.astra.cli.streaming;

import com.dtsx.astra.cli.core.AbstractConnectedCmd;
import com.github.rvesse.airline.annotations.Arguments;
import com.github.rvesse.airline.annotations.restrictions.Required;

/**
 * Superclass when working with tenant.
 */
public abstract class AbstractStreamingCmd extends AbstractConnectedCmd {

    /** name of the DB. */
    @Required
    @Arguments(title = "TENANT", description = "Tenant identifier")
    protected String tenant;

}
