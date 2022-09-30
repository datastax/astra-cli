package com.datastax.astra.cli.core;

import com.datastax.astra.cli.core.exception.FileSystemException;
import com.datastax.astra.cli.core.exception.InvalidTokenException;
import com.datastax.astra.cli.core.exception.TokenNotFoundException;
import com.datastax.astra.cli.utils.AstraRcUtils;
import com.github.rvesse.airline.annotations.Option;

/**
 * Base command for cli. The cli have to deal with configuration file and initialize connection
 * each tiem where shell already have the context initialized.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public abstract class AbstractConnectedCmd extends AbstractCmd {
    
    /** Authentication token used if not provided in config. */
    @Option(name = { "--token" }, 
            title = "AUTH_TOKEN",
            description = "Key to use authenticate each call.")
    protected String token;

    /** Section. */
    @Option(name = { "-conf","--config" }, 
            title = "CONFIG_SECTION",
            description= "Section in configuration file (default = ~/.astrarc)")
    protected String configSectionName = AstraRcUtils.ASTRARC_DEFAULT;
    
    /** {@inheritDoc} */
    @Override
    public void init() 
    throws TokenNotFoundException, InvalidTokenException, FileSystemException {
        ctx().init(this);
        if (null == ctx().getToken()) {
           throw new TokenNotFoundException();
        }
    }
    
    /**
     * Getter accessor for attribute 'token'.
     *
     * @return
     *       current value of 'token'
     */
    public String getToken() {
        return token;
    }
    
    /**
     * Change options format.
     * 
     * @param pToken
     *      current token
     * @return
     *      current reference
     */
    public AbstractConnectedCmd token(String pToken) {
        this.token = pToken;
        return this;
    }

    /**
     * Getter accessor for attribute 'configSectionName'.
     *
     * @return
     *       current value of 'configSectionName'
     */
    public String getConfigSectionName() {
        return configSectionName;
    } 
    
    /**
     * Change options format.
     * 
     * @param pSection
     *      current section
     * @return
     *      current reference
     */
    public AbstractConnectedCmd section(String pSection) {
        this.configSectionName = pSection;
        return this;
    }
   
}
