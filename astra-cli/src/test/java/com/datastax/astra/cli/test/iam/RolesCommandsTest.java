package com.datastax.astra.cli.test.iam;

import org.junit.jupiter.api.Test;

import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Testing CRUD for roles.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class RolesCommandsTest extends AbstractCmdTest {

    
    @Test
    public void listRoles()  throws Exception {
        astraCli("role", AbstractCmd.LIST, "-f", "json" , "--verbose");
    }
    
    @Test
    public void showRole() throws Exception {
        astraCli("role", AbstractCmd.GET, "dde8a0e9-f4ae-4b42-b642-9f257436c8da");
    }
    
    @Test
    public void showRoleCsv() throws Exception {
        astraCli("role", AbstractCmd.GET, "dde8a0e9-f4ae-4b42-b642-9f257436c8da", "-f", "csv");
    }
    
    @Test
    public void showRoleJson() throws Exception {
        astraCli("role", "show", "dde8a0e9-f4ae-4b42-b642-9f257436c8da", "-f", "json");
    }
    
}
