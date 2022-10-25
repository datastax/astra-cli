package com.datastax.astra.cli.streaming.pulsarshell;

/*-
 * #%L
 * Astra Cli
 * %%
 * Copyright (C) 2022 DataStax
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

/**
 * Options for Pulsar Shell CLI
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class PulsarShellOptions {

    /** option. */
    private String execute;
    
    /** option. */
    protected boolean failOnError = false;
    
    /** option. */
    protected String fileName;
    
    /** option. */
    protected boolean noProgress = false;

    /**
     * Getter accessor for attribute 'execute'.
     *
     * @return
     *       current value of 'execute'
     */
    public String getExecute() {
        return execute;
    }

    /**
     * Setter accessor for attribute 'execute'.
     * @param execute
     * 		new value for 'execute '
     */
    public void setExecute(String execute) {
        this.execute = execute;
    }

    /**
     * Getter accessor for attribute 'failOnError'.
     *
     * @return
     *       current value of 'failOnError'
     */
    public boolean isFailOnError() {
        return failOnError;
    }

    /**
     * Setter accessor for attribute 'failOnError'.
     * @param failOnError
     * 		new value for 'failOnError '
     */
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * Getter accessor for attribute 'fileName'.
     *
     * @return
     *       current value of 'fileName'
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Setter accessor for attribute 'fileName'.
     * @param fileName
     * 		new value for 'fileName '
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Getter accessor for attribute 'noProgress'.
     *
     * @return
     *       current value of 'noProgress'
     */
    public boolean isNoProgress() {
        return noProgress;
    }

    /**
     * Setter accessor for attribute 'noProgress'.
     * @param noProgress
     * 		new value for 'noProgress '
     */
    public void setNoProgress(boolean noProgress) {
        this.noProgress = noProgress;
    }
   
}
