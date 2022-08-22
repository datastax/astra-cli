package com.datastax.astra.shell.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utililities for the command line.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class CommandLineUtils {
    
    /** Delimiter for an argument. */
    public static final char DELIMITER = ' ';
    
    /** If a character should not be interpreted. */
    public static final char ESCAPER   = '\\';
    
    /** Escaping a character with quotes. */
    public static final List<Character> QUOTES = Arrays.asList('\'', '\"');
    
    /**
     * Hide default constructor.
     */
    private CommandLineUtils() {}
    
    /**
     * Custom parsing of command escaping characters.
     *
     * @param command
     *      current commant
     * @return
     *      arguments of the command
     */
    public static String[] parseCommand(String command) {
        if (command == null) return new String[0];
        List<String> chunks = new ArrayList<>();
        Character argQuote = null;
        StringBuilder arg = new StringBuilder();
        for(int i =0;i<command.length();i++) {
            char c = command.charAt(i);
            switch(c) {
                case DELIMITER:
                    if (argQuote != null) {
                        // As the argument has a quote, space is not end
                        arg.append(c);
                    } else if (arg.length() > 0) {
                        // End of Argument (if argument not empty)
                        chunks.add(arg.toString());
                        arg = new StringBuilder();
                    }
                break;
                case ESCAPER:
                    if (i<command.length()-1) {
                        i++;
                        arg.append(command.charAt(i));
                    }
                break;
                default:
                    if (QUOTES.contains(c)) {
                        if (argQuote == null) {
                            // Beginning of quoted argument
                            argQuote = c;
                        } else if (argQuote == c) {
                            // End of Argument (same quote)
                            argQuote = null;
                            chunks.add(arg.toString());
                            arg = new StringBuilder();
                        } else {
                            // Quote ' in the middle of an argument quoted with another quot
                            arg.append(c);
                        }
                    } else {
                        arg.append(c);
                    }
                break;
            }
        }
        // Terminal element of the command
        if (arg.length() > 0) {
            chunks.add(arg.toString());
        }
        return chunks.toArray(new String[0]);
    }
    
}
