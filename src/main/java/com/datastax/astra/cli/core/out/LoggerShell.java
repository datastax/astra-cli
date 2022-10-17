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
	
    /** Using sl4j to access console, eventually pushing to file as well. */
    private static Logger LOGGER = LoggerFactory.getLogger(LoggerShell.class);
    
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
        if (ctx().getOutputFormat().equals(OutputFormat.human)) {
            if (CliContext.getInstance().isNoColor()) {
                LOGGER.info("[INFO ] - " + text);
            } else {
                LOGGER.info(ansi().fg(GREEN).a("[ INFO ] - ").reset().a(text).toString());
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
        if (ctx().isNoColor() && ctx().getOutputFormat().equals(OutputFormat.human)) {
            LOGGER.info("[ERROR] - " + text);
        } else {
            LOGGER.info(ansi().fg(RED)
                    .a("[ERROR] - ").reset()
                    .a(text).toString());
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
       if (customMessage != null) {
           error(customMessage);
       }
       error(e.getMessage() + " ("+ e.getClass().getSimpleName() + ")");
       
       error("Try 'astra help " + cmd + "' to get help");
       if (ctx().isVerbose()) {
           Arrays.asList(e.getStackTrace())
                 .stream()
                 .forEach(System.out::println);
       }
    }
    
    /**
     * Log warning.
     *
     * @param text
     *       text to be displayed
     */
    public static void warning(String text) {
        if (ctx().isNoColor() && ctx().getOutputFormat().equals(OutputFormat.human)) {
            LOGGER.info("[WARN ] - " + text);
        } else {
            LOGGER.info(ansi().fg(YELLOW).a("[WARN ] - ").reset().a(text).toString());
        }
    }
    
    /**
     * Syntax sugar for OK.
     * 
     * @param text
     *      text to show in success
     */
    public static void debug(String text) {
        if (ctx().isVerbose() && ctx().getOutputFormat().equals(OutputFormat.human)) {
            if (ctx().isNoColor()) {
                LOGGER.info("[DEBUG] - " + text);
            } else {
                LOGGER.info(ansi().fg(YELLOW).a("[DEBUG] - ").reset().a(text).toString());
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
        if (ctx().isVerbose() && ctx().getOutputFormat().equals(OutputFormat.human)) {
            if (ctx().isNoColor()) {
                LOGGER.info("[INFO ] - " + text);
            } else {
                LOGGER.info(ansi().fg(CYAN).a("[INFO ] - ").reset().a(text).toString());
            }
        }
    }
    
    /**
     * Get context.
     *
     * @return
     *      cli context
     */
    private static CliContext ctx() {
        return CliContext.getInstance();
    }
    
}
