package com.datastax.astra.cli.core.out;

import static org.fusesource.jansi.Ansi.ansi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.ShellContext;
import com.datastax.astra.cli.utils.AstraCliUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Render all component for the FF4J commands.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class AstraCliConsole {
    
    /** Using sl4j to access console, eventually pushing to file as well. */
    private static Logger LOGGER = LoggerFactory.getLogger(AstraCliConsole.class);
    
    /** Json Object Mapper. */
    public static final ObjectMapper OM = new ObjectMapper();
    
	/** Default constructor. */
	private AstraCliConsole() {}
	
	/** Start Banner. */
    public static void banner() {
        println("");
        println("  █████╗ ███████╗████████╗██████╗  █████╗   ", Ansi.Color.MAGENTA);
        println(" ██╔══██╗██╔════╝╚══██╔══╝██╔══██╗██╔══██╗  ", Ansi.Color.MAGENTA);
        println(" ███████║███████╗   ██║   ██████╔╝███████║  ", Ansi.Color.MAGENTA);
        println(" ██╔══██║╚════██║   ██║   ██╔══██╗██╔══██║  ", Ansi.Color.MAGENTA);
        println(" ██║  ██║███████║   ██║   ██║  ██║██║  ██║  ", Ansi.Color.MAGENTA);
        println(" ╚═╝  ╚═╝╚══════╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝  ", Ansi.Color.MAGENTA);
        println("");
        println(" Version: " + AstraCliUtils.version() + "\n", Ansi.Color.CYAN);
    }
    
    /**
     * Output.
     *
     * @param text
     *      text to display
     */
    public static void println(String text) {
        LOGGER.info(text);
    }
    
    /**
     * Output.
     *
     * @param text
     *      text to display
     * @param color
     *      color for the text
     */
    public static void println(String text, Ansi.Color color) {
        if (ctx().isNoColor()) {
            LOGGER.info(text);
        } else {
            LOGGER.info(ansi().fg(color).a(text).reset().toString());
        }
    }
    
    /**
     * Output.
     *
     * @param builder
     *      current builder
     */
    public static void println(StringBuilderAnsi builder) {
        LOGGER.info(builder.toString());
    }
    
    /**
     * Log as JSON in the console.
     *
     * @param json
     *      json in the console
     */
    public static void printJson(JsonOutput json) {
        if (json != null) {
            try {
                println(OM.writerWithDefaultPrettyPrinter()
                              .writeValueAsString(json));
            } catch (JsonProcessingException e) {
                LoggerShell.error("Cannot create JSON :" + e.getMessage());
            }
        }
    }
    
    /**
     * Log as CSV in the output.
     *
     * @param csv
     *      create CSV for the output
     */
    public static void printCsv(CsvOutput csv) {
        if (csv != null) {
            println(csv.toString());
        }
    }
    
    /**
     * Show the table in console.
     * 
     * @param sht
     *      table
     */
    public static void printShellTable(ShellTable sht) {
        switch(ctx().getOutputFormat()) {
            case json:
                sht.showJson();
            break;
            case csv: 
                sht.showCsv(); 
            break;
            case human:
            default:
                sht.show();
            break;
        }
    }
    
    /**
     * Show object as Json in console.
     *
     * @param obj
     *      object
     * @param color
     *      color
     */
    public static final void printObjectAsJson(Object obj, Ansi.Color color) {
        try {
            println(OM
                  .writerWithDefaultPrettyPrinter()
                  .writeValueAsString(obj), color);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialize object as JSON", e);
        }
    }
    
	/**
	 * Will print Promt based on the current state.
	 */
	public static void prompt() {
	    println("");
	    ShellContext ctx = ShellContext.getInstance();
	    StringBuilderAnsi builder = new StringBuilderAnsi();
	    if (ctx.getOrganization() != null) {
	        builder.append(ctx.getOrganization().getName(), Ansi.Color.GREEN);
	    }
	    if (ctx.getDatabase() != null) {
	        builder.append(" > ", Ansi.Color.GREEN);
	        builder.append(ctx.getDatabase().getInfo().getName(), Ansi.Color.YELLOW);
	        builder.append(" > ", Ansi.Color.GREEN);
	        builder.append(ctx.getDatabaseRegion() + " ", Ansi.Color.YELLOW);
        }
	    builder.append("> ", Ansi.Color.GREEN);
	    System.out.print(builder.toString());
	}
	
	/**
     * Exit program with error.
     *
     * @param code
     *      error code
     * @param msg
     *      error message
     */
    public static void outputError(ExitCode code, String msg) {
        switch(ctx().getOutputFormat()) {
            case json:
                printJson(new JsonOutput(code, code.name() + ": " + msg));
            break;
            case csv:
                printCsv(new CsvOutput(code,  code.name() + ": " + msg));
            break;
            case human:
            default:
                LoggerShell.error(code.name() + ": " + msg);
            break;
        }
    }
    
    /**
     * Exit program with no operation
     *
     * @param code
     *      error code
     * @param msg
     *      error message
     */
    public static void outputWarning(ExitCode code, String msg) {
        switch(ctx().getOutputFormat()) {
            case json:
                printJson(new JsonOutput(code, code.name() + ": " + msg));
            break;
            case csv:
                printCsv(new CsvOutput(code,  code.name() + ": " + msg));
            break;
            case human:
            default:
                LoggerShell.warning(code.name() + ": " + msg);
            break;
        }
    }
    
    /**
     * Exit program with error.
     *
     * @param label
     *      error label
     * @param data
     *      show data
     */
    public static void outputData(String label, String data) {
        switch(ctx().getOutputFormat()) {
            case json:
                printJson(new JsonOutput(ExitCode.SUCCESS, label, data));
            break;
            case csv:
                Map<String, String> m = new HashMap<>();
                m.put(label, data);
                printCsv(new CsvOutput(Arrays.asList(label), Arrays.asList(m)));
            break;
            case human:
            default:
               System.out.println(data);
            break;
        }
    }
    
    /**
     * Exit program with error.
     *
     * @param msg
     *      return message
     */
    public static void outputSuccess(String msg) {
        switch(ctx().getOutputFormat()) {
            case json:
                printJson(new JsonOutput(ExitCode.SUCCESS, msg));
            break;
            case csv:
                printCsv(new CsvOutput(ExitCode.SUCCESS, msg));
            break;
            case human:
            default:
                LoggerShell.success(msg);
            break;
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
