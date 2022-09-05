package com.datastax.astra.shell.cmd.shell;

import com.github.rvesse.airline.annotations.Command;

/**
 * Class to TODO
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "<empty>", description = "New line")
public class EmptySh implements Runnable {

    /** {@inheritDoc} */
    @Override
    public void run() {
        System.out.println("");
    }
    

}
