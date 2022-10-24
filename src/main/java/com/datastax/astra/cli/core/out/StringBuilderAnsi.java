package com.datastax.astra.cli.core.out;

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
