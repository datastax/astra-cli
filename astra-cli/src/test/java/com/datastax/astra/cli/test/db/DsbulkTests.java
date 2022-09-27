package com.datastax.astra.cli.test.db;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.datastax.astra.cli.db.dsbulk.DsBulkUtils;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Tests DSBulk.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class DsbulkTests extends AbstractCmdTest {
    
    @Test
    public void should_install_DsBulk()  throws Exception {
        DsBulkUtils.installDsBulk();
        Assertions.assertTrue(DsBulkUtils.isDsBulkInstalled());
    }
    
    @Test
    public void should_run() {
        astraCli("db dsbulk workshops "
                + "load -url /Users/cedricklunven/dev/workspaces/datastax-workshops/workshop-introduction-to-machine-learning/jupyter/data/ratings.csv "
                + "-k machine_learning "
                + "-t movieratings "
                + "-m \"userid,movieid,rating,timestamp\" "
                + "-header false -delim -c csv");
    }

}
