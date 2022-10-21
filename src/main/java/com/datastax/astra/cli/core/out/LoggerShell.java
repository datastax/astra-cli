package com.datastax.astra.cli.core.out;

import com.datastax.astra.cli.core.CliContext;
import org.fusesource.jansi.Ansi;

import java.util.Arrays;

import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;

/**
 * Work with terminal.
 *
 * @author Cedrick Lunven (@clunven)
 */
public class LoggerShell extends AstraCliConsole {

    /** Info String. */
    private static final String INFO  = "[INFO]  ";
    
    /** Info String. */
    private static final String INFO_CYAN = ansi().fg(CYAN).a(INFO).reset().toString();

    /** Error String. */
    private static final String ERROR = "[ERROR] ";
    
    /** Info String. */
    private static final String ERROR_RED = ansi().fg(RED).a(ERROR).reset().toString();
    
    /** Warning String. */
    private static final String WARNING = "[WARN]  ";
    
    /** Info String. */
    private static final String WARNING_YELLOW = ansi().fg(YELLOW).a(WARNING).reset().toString();
    
    /** Info String. */
    private static final String SUCCESS  = "[OK]    ";
    
    /** Info String. */
    private static final String SUCCESS_GREEN =  ansi().fg(GREEN).a(SUCCESS).reset().toString();
    
    /** Warning String. */
    private static final String DEBUG = "[DEBUG] ";

	/**
	 * Hide default  constructor.
	 */
	protected LoggerShell() {
        super();
    }

    /**
     * Syntax sugar for OK.
     * 
     * @param text
     *      text to show in success
     */
    public static void success(String text) {
        if (isHuman()) {
            if (isColor()) {
                println(SUCCESS_GREEN + text);
            } else {
                println(SUCCESS + text);
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
                println(INFO_CYAN + text);
            } else {
                println(INFO + text);
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
                println(ERROR_RED + text);
            } else {
                println(ERROR + text);
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
                print(WARNING_YELLOW + text);
            } else {
                print(WARNING + text);
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
            println(DEBUG + text);
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
     *      if you should print because human output
     */
    private static boolean isHuman() {
        return CliContext.getInstance()
                         .getOutputFormat()
                         .equals(OutputFormat.HUMAN);
    }
    
    
    /**
     * Get context.
     *
     * @return
     *      if you should print because human output
     */
    private static boolean isColor() {
        return !CliContext.getInstance()
                         .isNoColor();
    }
    
}
