package com.dtsx.astra.cli.core.out;

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

import static org.fusesource.jansi.Ansi.ansi;

import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

/**
 * String Builder with colors.
 * 
 * @author Cedrick LUNVEN (@clunven)
 */
public class StringBuilderAnsi {
    
    /** Internal String Builder. */
    private final StringBuilder builder;
    
    /**
     * Default constructor
     */
    public StringBuilderAnsi() {
        builder = new StringBuilder();
    }
   
    /**
     * Start with a string.
     * 
     * @param init
     *      current string
     */
    public StringBuilderAnsi(String init) {
        builder = new StringBuilder(init);
    }

    /**
     * Text in color in the console.
     *
     * @param text
     *      current text.
     * @param color
     *      current color
     * @return
     *      text colored
     */
    private String colored(String text, Ansi.Color color) {
        return ansi().fg(color).a(text).reset().toString();
    }

    /**
     * Append colored content.
     * 
     * @param text
     *      target text
     * @param color
     *      target color
     * @return
     *      current reference
     */
    public StringBuilderAnsi append(String text, Ansi.Color color) {
        builder.append(colored(text, color));
        return this;
    }
    
    /**
     * Append colored content with size.
     *
     * @param text
     *      target text
     * @param color
     *      current color
     * @param size
     *      size of element
     */
    public void append(String text, Ansi.Color color, int size) {
        builder.append(colored(StringUtils.rightPad(text,size), color));
    }

    /** {@inheritDoc} */
    public String toString() {
        return builder.toString();
    }

}
