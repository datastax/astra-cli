package com.datastax.astra.cli.core.out;

import static org.fusesource.jansi.Ansi.ansi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.Ansi;

import com.datastax.astra.cli.ExitCode;
import com.datastax.astra.cli.ShellContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Render all component for the FF4J commands.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class ShellPrinter {
    
	/** Default constructor. */
	private ShellPrinter() {}
	
	/** Start Banner. */
    public static void banner() {
        System.out.println();
        System.out.print("  █████╗ ███████╗████████╗██████╗  █████╗   ");
        System.out.println("  ███████╗██╗  ██╗███████╗██╗     ██╗     ");
        System.out.print(" ██╔══██╗██╔════╝╚══██╔══╝██╔══██╗██╔══██╗  ");
        System.out.println("  ██╔════╝██║  ██║██╔════╝██║     ██║  ");
        System.out.print(" ███████║███████╗   ██║   ██████╔╝███████║  ");
        System.out.println("  ███████╗███████║█████╗  ██║     ██║   ");
        System.out.print(" ██╔══██║╚════██║   ██║   ██╔══██╗██╔══██║  ");
        System.out.println("  ╚════██║██╔══██║██╔══╝  ██║     ██║");
        System.out.print(" ██║  ██║███████║   ██║   ██║  ██║██║  ██║  ");
        System.out.println("  ███████║██║  ██║███████╗███████╗███████╗");
        System.out.print(" ╚═╝  ╚═╝╚══════╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝  ");
        System.out.println("  ╚══════╝╚═╝  ╚═╝╚══════╝╚══════╝╚══════╝");
        System.out.println("");
        System.out.print(" Version: " + version() + "\n");
    }
    
    /**
     * Show version.
     *
     * @return
     *      return version
     */
    public static String version() {
        String versionPackage = ShellPrinter.class
                .getPackage()
                .getImplementationVersion();
        if (versionPackage == null) {
            versionPackage = "Development";
        }
        return versionPackage;
    }
    
    /**
     * Json Object Mapper. 
     */
    public static final ObjectMapper OM = new ObjectMapper();
    
    /**
     * Output.
     *
     * @param text
     *      text to display
     * @param color
     *      colot
     */
    public static void print(String text, Ansi.Color color) {
        if (ctx().isNoColor()) {
            System.out.print(text);
        } else {
            System.out.print(ansi().fg(color).a(text).reset());
        }
    }
    
    /**
     * Output.
     *
     * @param text
     *      text to display
     * @param color
     *      colot
     */
    public static void println(String text, Ansi.Color color) {
        if (ctx().isNoColor()) {
            System.out.println(text);
        } else {
            System.out.println(ansi().fg(color).a(text).reset());
        }
    }
    
    /**
     * Show text in the console.
     * 
     * @param text
     *      content of the message
     * @param size
     *      text size
     * @param color
     *      text color
     */
    public static void print(String text, Ansi.Color color, int size) {
        print(StringUtils.rightPad(text, size), color);
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
                String myJson = OM
                        .writerWithDefaultPrettyPrinter()
                        .writeValueAsString(json);
                System.out.println(myJson);
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
            System.out.println(csv.toString());
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
	    System.out.println("");
	    ShellContext ctx = ShellContext.getInstance();
	    if (ctx.getOrganization() != null) {
	        print(ctx.getOrganization().getName(), Ansi.Color.GREEN);
	    }
	    if (ctx.getDatabase() != null) {
	        print(" > ", Ansi.Color.GREEN);
            print(ctx.getDatabase().getInfo().getName(), Ansi.Color.YELLOW);
            print(" > ", Ansi.Color.GREEN);
            print(ctx.getDatabaseRegion() + " ", Ansi.Color.YELLOW);
        }
	    print("> ", Ansi.Color.GREEN);
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
