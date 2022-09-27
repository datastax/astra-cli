package com.datastax.astra.cli.test.iam;

import org.junit.jupiter.api.Test;

import com.datastax.astra.cli.test.AbstractCmdTest;

/**
 * Commands relative to users.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class UsersCommandsTest extends AbstractCmdTest {

    @Test
    public void showUsers()  throws Exception {
        astraCli("user", "list");
    }
    
    @Test
    public void showUser()  throws Exception {
        astraCli("user", "show", "cedrick.lunven@datastax.com");
    }
    
    @Test
    public void userInvite()  throws Exception {
        astraCli("user", "invite", "david.dieruf@datastax.com");
    }
    
    @Test
    public void userDelete()  throws Exception {
        astraCli("user", "delete", "celphys@gmail.com");
    }
    
}
