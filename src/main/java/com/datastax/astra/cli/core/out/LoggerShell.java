package com.datastax.astra.cli.core.out;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.CYAN;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.Color.YELLOW;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.astra.cli.core.CliContext;

/**
 * Work with terminal.
 *
 * @author Cedrick Lunven (@clunven)
 */
public class LoggerShell {
    
    /** Info String. */
    private static final String INFO  = "[INFO]  - ";
    
    /** Info String. */
    private static final String INFO_CYAN = ansi().fg(CYAN).a(INFO).reset().toString();
    
    /** Error String. */
    private static final String ERROR = "[ERROR] - ";
    
    /** Info String. */
    private static final String ERROR_RED = ansi().fg(RED).a(ERROR).reset().toString();
    
    /** Warning String. */
    private static final String WARNING = "[WARN]  - ";
    
    /** Info String. */
    private static final String WARNING_YELLOW = ansi().fg(YELLOW).a(WARNING).reset().toString();
    
    /** Info String. */
    private static final String SUCCESS  = "[OK]    - ";
    
    /** Info String. */
    private static final String SUCCESS_GREEN = ansi().fg(GREEN).a(GREEN).reset().toString();
    
    /** Warning String. */
    private static final String DEBUG = "[DEBUG] - ";
    
    /** Using sl4j to access console, eventually pushing to file as well. */
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerShell.class);
    
	/**
	 * Hide default  constructor.
	 */
	private LoggerShell() {}
	
    /**
     * Syntax sugar for OK.
     * 
     * @param text
     *      text to show in success
     */
    public static void success(String text) {
        if (isHuman()) {
            if (isColor()) {
                LOGGER.info(SUCCESS_GREEN + text);
            } else {
                LOGGER.info(SUCCESS + text);
            }
        }
    }
    
    /**
     * Syntax sugar for OK.
     *
     * @param text
     *      text to show in success
     */
    public static void info(String text) {
        if (isHuman()) {
            if (isColor()) {
                LOGGER.info(INFO_CYAN + text);
            } else {
                LOGGER.info(INFO + text);
            }
        }
    }
    
    /**
     * Log error.
     *
     * @param text
     *       text to be displayed
     */
    public static void error(String text) {
        if (isHuman()) {
            if (isColor()) {
                LOGGER.info(ERROR_RED + text);
            } else {
                LOGGER.info(ERROR + text);
            }
        }
    }
    
    /**
     * Log warning.
     *
     * @param text
     *       text to be displayed
     */
    public static void warning(String text) {
        if (isHuman()) {
            if (isColor()) {
                LOGGER.info(WARNING_YELLOW + text);
            } else {
                LOGGER.info(WARNING + text);
            }
        }
    }
    
    /**
     * Syntax sugar for OK.
     * 
     * @param text
     *      text to show in success
     */
    public static void debug(String text) {
        if (CliContext.getInstance().isVerbose()) {
            LOGGER.info(DEBUG + text);
        }
    }
    
    /**
     * Log error.
     * 
     * @param e
     *      exception
     * @param cmd
     *      current command 
     * @param customMessage
     *      current error message
     */
    public static void exception (Exception e, String cmd, String customMessage) {
       if (customMessage != null)
           error(customMessage);
       error(e.getMessage() + " ("+ e.getClass().getSimpleName() + ")");
       error("Try 'astra help " + cmd + "' to get help");
       if (CliContext.getInstance().isVerbose())
           Arrays.stream(e.getStackTrace()).forEach(System.out::println);
    }
    
    /**
     * Get context.
     *
     * @return
     *      if you should print becasue human output
     */
    private static boolean isHuman() {
        return CliContext.getInstance()
                         .getOutputFormat()
                         .equals(OutputFormat.human);
    }
    
    
    /**
     * Get context.
     *
     * @return
     *      if you should print becasue human output
     */
    private static boolean isColor() {
        return !CliContext.getInstance()
                         .isNoColor();
    }
    
}
