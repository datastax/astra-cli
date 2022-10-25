package com.datastax.astra.cli.core.out;

/*-
 * #%L
 * Astra Cli
 * %%
 * Copyright (C) 2022 DataStax
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.datastax.astra.cli.core.CliContext;
import com.datastax.astra.cli.core.ExitCode;
import com.datastax.astra.cli.utils.AstraCliUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.fusesource.jansi.Ansi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.fusesource.jansi.Ansi.ansi;

/**
 * Render all component for the FF4J commands.
 *
 * @author Cedrick LUNVEN (@clunven)
 */
public class AstraCliConsole {

    /** Json Object Mapper. */
    public static final ObjectMapper OM = new ObjectMapper();
    
	/** Default constructor. */
	protected AstraCliConsole() {}
	
	/** Start Banner. */
    public static void banner() {
        println("");
        println("    _____            __                  ", 236, 107, 40);
        println("   /  _  \\   _______/  |_____________    ", 236, 107, 40);
        println("  /  /_\\  \\ /  ___/\\   __\\_  __ \\__  \\  ",236, 107, 40);
        println(" /    |    \\\\___ \\  |  |  |  | \\// __ \\_ ", 236, 107, 40);
        println(" \\____|__  /____  > |__|  |__|  (____  /", 236, 107, 40);
        println("         \\/     \\/                   \\/ ", 236, 107, 40);
        println("");
        println(" Version: " + AstraCliUtils.version() + "\n", Ansi.Color.CYAN);
    }

    /**
     * Access to Console.
     *
     * @param message
     *      message to print
     */
    public static void print(String message) {
        System.out.print(message);
    }
    
    /**
     * Output.
     *
     * @param text
     *      text to display
     */
    public static void println(String text) {
        System.out.println(text);
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
            print(text + System.lineSeparator());
        } else {
            print(ansi().fg(color).a(text).reset().toString() + System.lineSeparator());
        }
    }

    /**
     * Display item with colors.
     *
     * @param text
     *      text to display
     * @param red
     *      red
     * @param green
     *      green
     * @param blue
     *      blue
     */
    public static void println(String text, int red, int green, int blue) {
        if (ctx().isNoColor()) {
            print(text + System.lineSeparator());
        } else {
            print(ansi().fgRgb(red, green, blue).a(text).reset().toString() + System.lineSeparator());
        }
    }
    
    /**
     * Output.
     *
     * @param builder
     *      current builder
     */
    public static void println(StringBuilderAnsi builder) {
        println(builder.toString());
    }
    
    /**
     * Log as JSON in the console.
     *
     * @param json
     *      json in the console
     * @param <T>
     *     object type to marshall
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
            case JSON -> sht.showJson();
            case CSV -> sht.showCsv();
            case HUMAN -> sht.show();
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
            case JSON -> printJson(new JsonOutput<String>(code, code.name() + ": " + msg));
            case CSV -> printCsv(new CsvOutput(code, code.name() + ": " + msg));
            case HUMAN -> LoggerShell.error(code.name() + ": " + msg);
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
            case JSON -> printJson(new JsonOutput<>(ExitCode.SUCCESS, label, data));
            case CSV -> {
                Map<String, String> m = new HashMap<>();
                m.put(label, data);
                printCsv(new CsvOutput(List.of(label), List.of(m)));
            }
            case HUMAN -> println(data);
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
            case JSON -> printJson(new JsonOutput<String>(ExitCode.SUCCESS, msg));
            case CSV -> printCsv(new CsvOutput(ExitCode.SUCCESS, msg));
            case HUMAN -> LoggerShell.success(msg);
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
