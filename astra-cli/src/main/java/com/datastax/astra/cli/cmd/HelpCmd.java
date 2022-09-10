package com.datastax.astra.cli.cmd;

import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.help.Help;

/**
 * Display help.
 *
 * @author Cedrick LUNVEN (@clunven)
 *
 * @param <T>
 *      generic for the Help Command
 */
@Command(name = "help", description = "Print this help text and exit")
public class HelpCmd<T> extends Help<T> {
   
    /** {@inheritDoc} */
    @Override
    public void run() {
        super.run();
    }
}