package com.datastax.astra.cli.config;


import com.datastax.astra.cli.core.AbstractCmd;
import com.datastax.astra.cli.core.exception.InvalidTokenException;
import com.datastax.astra.cli.core.exception.TokenNotFoundException;

/**
 * Parent class for config.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class AbstractConfigCmd extends AbstractCmd {
    
    /** {@inheritDoc} */
    @Override
    public void init() 
    throws TokenNotFoundException, InvalidTokenException {
       ctx().init(this);
    }

}
