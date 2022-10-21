package com.datastax.astra.cli.core.out;

import static org.fusesource.jansi.Ansi.ansi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.fusesource.jansi.Ansi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.astra.cli.core.CliContext;
import com.datastax.astra.cli.core.ExitCode;
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
    private static final Logger LOGGER = LoggerFactory.getLogger(AstraCliConsole.class);
    
    /** Json Object Mapper. */
    public static final ObjectMapper OM = new ObjectMapper();
    
	/** Default constructor. */
	private AstraCliConsole() {}
	
	/** Start Banner. */
    public static void banner() {
        println("");
        println("    _____            __                  ", Ansi.Color.GREEN);
        println("   /  _  \\   _______/  |_____________    ", Ansi.Color.GREEN);
        println("  /  /_\\  \\ /  ___/\\   __\\_  __ \\__  \\  ", Ansi.Color.GREEN);
        println(" /    |    \\\\___ \\  |  |  |  | \\// __ \\_ ", Ansi.Color.GREEN);
        println(" \\____|__  /____  > |__|  |__|  (____  /", Ansi.Color.GREEN);
        println("         \\/     \\/                   \\/ ", Ansi.Color.GREEN);
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
    public static <T> void printJson(JsonOutput<T> json) {
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
        switch (ctx().getOutputFormat()) {
            case json -> sht.showJson();
            case csv  -> sht.showCsv();
            case human -> sht.show();
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
    public static void printObjectAsJson(Object obj, Ansi.Color color) {
        try {
            println(OM
                  .writerWithDefaultPrettyPrinter()
                  .writeValueAsString(obj), color);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialize object as JSON", e);
        }
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
        switch (ctx().getOutputFormat()) {
            case json -> printJson(new JsonOutput<String>(code, code.name() + ": " + msg));
            case csv -> printCsv(new CsvOutput(code, code.name() + ": " + msg));
            case human -> LoggerShell.error(code.name() + ": " + msg);
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
        switch (ctx().getOutputFormat()) {
            case json -> printJson(new JsonOutput<String>(code, code.name() + ": " + msg));
            case csv -> printCsv(new CsvOutput(code, code.name() + ": " + msg));
            case human -> LoggerShell.warning(code.name() + ": " + msg);
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
        switch (ctx().getOutputFormat()) {
            case json -> printJson(new JsonOutput<String>(ExitCode.SUCCESS, label, data));
            case csv -> {
                Map<String, String> m = new HashMap<>();
                m.put(label, data);
                printCsv(new CsvOutput(Arrays.asList(label), Arrays.asList(m)));
            }
            case human -> System.out.println(data);
        }
    }
    
    /**
     * Exit program with error.
     *
     * @param msg
     *      return message
     */
    public static void outputSuccess(String msg) {
        switch (ctx().getOutputFormat()) {
            case json  -> printJson(new JsonOutput<String>(ExitCode.SUCCESS, msg));
            case csv   -> printCsv(new CsvOutput(ExitCode.SUCCESS, msg));
            case human -> LoggerShell.success(msg);
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
