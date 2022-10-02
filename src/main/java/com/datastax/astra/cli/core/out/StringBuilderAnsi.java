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
    private StringBuilder builder;
    
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
     * Start with a colored String.
     *
     * @param init
     *      default text
     * @param color
     *      default color
     */
    public StringBuilderAnsi(String init, Ansi.Color color) {
        builder = new StringBuilder(colored(init, color));
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
     * Append content.
     * 
     * @param pObject
     *      target object
     * @return
     *      current reference
     */
    public StringBuilderAnsi append(Object pObject) {
        builder.append(pObject);
        return this;
    }
    
    /**
     * Append content.
     * 
     * @param text
     *      target text
     * @return
     *      current reference
     */
    public StringBuilderAnsi append(String text) {
        builder.append(text);
        return this;
    }
    
    /**
     * Append colored content.
     * 
     * @param pObject
     *      target object
     * @param color
     *      target color
     * @return
     *      current reference
     */
    public StringBuilderAnsi append(Object pObject, Ansi.Color color) {
        builder.append(colored(String.valueOf(pObject), color));
        return this;
    }
    
    /**
     * Append right pad.
     * 
     * @param text
     *      current text to append
     * @param size
     *      global size of the message
     * @return
     *      current reference
     */
    public StringBuilderAnsi append(String text, int size) {
        builder.append(StringUtils.rightPad(text,size));
        return this;
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
     * @return
     *      current reference
     */
    public StringBuilderAnsi append(String text, Ansi.Color color, int size) {
        builder.append(colored(StringUtils.rightPad(text,size), color));
        return this;
    }
    
   
    
    /** {@inheritDoc} */
    public String toString() {
        return builder.toString();
    }

}
