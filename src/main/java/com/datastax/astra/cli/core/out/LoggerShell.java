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

import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.core.AbstractCmd;

/**
 * Work with terminal.
 *
 * @author Cedrick Lunven (@clunven)
 */
public class LoggerShell {
	
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
                System.out.println("Writes in log file failed: " + e.getMessage());
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
                System.out.println("[INFO ] - " + text);
            } else {
                System.out.println(ansi().fg(GREEN).a("[ INFO ] - ").reset().a(text));
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
            System.out.println("[ERROR] - " + text);
        } else {
            System.out.println(ansi().fg(RED).a("[ERROR] - ").reset().a(text));
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
            System.out.println("[WARN ] - " + text);
        } else {
             System.out.println(ansi().fg(YELLOW).a("[WARN ] - ").reset().a(text));
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
                System.out.println("[DEBUG] - " + text);
            } else {
                System.out.println(ansi().fg(YELLOW).a("[DEBUG] - ").reset().a(text));
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
                System.out.println("[INFO ] - " + text);
            } else {
                System.out.println(ansi().fg(CYAN).a("[INFO ] - ").reset().a(text));
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
