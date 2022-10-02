package com.datastax.astra.cli.core.out;

import static org.fusesource.jansi.Ansi.ansi;
import static org.fusesource.jansi.Ansi.Color.CYAN;
import static org.fusesource.jansi.Ansi.Color.GREEN;
import static org.fusesource.jansi.Ansi.Color.RED;
import static org.fusesource.jansi.Ansi.Color.YELLOW;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.core.AbstractCmd;

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
     * If log provided the output will go to the logfile.
     * 
     * @param level
     *      level to log
     * @param text
     *      text to log
     */
    private static void logToFile(String level, String text) {
        AbstractCmd cli = ShellContext.getInstance().getStartCommand();
        if (cli.getLogFileWriter() != null) {
            try {
                cli.getLogFileWriter().write(new Date().toString() 
                        + " - " 
                        + StringUtils.rightPad(level, 5) 
                        + " - " + text + System.lineSeparator());
                cli.getLogFileWriter().flush();
            } catch (IOException e) {
                LOGGER.error("Writes in log file failed: " + e.getMessage());
            }
        }
    }
    
    /**
     * Syntax sugar for OK.
     * 
     * @param text
     *      text to show in success
     */
    public static void success(String text) {
        
        if (ctx().getOutputFormat().equals(OutputFormat.human)) {
            if (ShellContext.getInstance().isNoColor()) {
                LOGGER.info("[INFO ] - " + text);
            } else {
                LOGGER.info(ansi().fg(GREEN).a("[ INFO ] - ").reset().a(text).toString());
            }
        }
        
        if (ShellContext.getInstance().isFileLoggerEnabled()) {
            logToFile("INFO", text);
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
        
        if (ctx().isFileLoggerEnabled()) {
            logToFile("ERROR", text);
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
        if (ctx().isFileLoggerEnabled()) {
            logToFile("WARN", text);
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
        
        if (ctx().isFileLoggerEnabled()) {
            logToFile("DEBUG", text);
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
        
        if (ctx().isFileLoggerEnabled()) {
            logToFile("INFO", text);
        }
    }
    
    /**
     * Get context.
     *
     * @return
     *      cli context
     */
    private static ShellContext ctx() {
        return ShellContext.getInstance();
    }
    
}
