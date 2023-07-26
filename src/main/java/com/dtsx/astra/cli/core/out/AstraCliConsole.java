package com.dtsx.astra.cli.core.out;

/*-
 * #%L
 * Astra CLI
 * --
 * Copyright (C) 2022 - 2023 DataStax
 * --
 * Licensed under the Apache License, Version 2.0
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.ExitCode;
import com.dtsx.astra.cli.utils.AstraCliUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dtsx.astra.cli.core.out.AstraAnsiColors.PURPLE_300;
import static org.fusesource.jansi.Ansi.ansi;
/**
 * Render all component for the FF4J commands.
 */
public class AstraCliConsole {

    /** Json Object Mapper. */
    public static final ObjectMapper OM = new ObjectMapper();
    
	/** Default constructor. */
	protected AstraCliConsole() {}

    public static void printVersion() {
        println(StringUtils.leftPad("Version: " + AstraCliUtils.version(), 40) + "\n", PURPLE_300);
    }
	/** Start Banner. */
    public static void banner() {
        println("    _____            __                  ", PURPLE_300);
        println("   /  _  \\   _______/  |_____________    ", PURPLE_300);
        println("  /  /_\\  \\ /  ___/\\   __\\_  __ \\__  \\  ", PURPLE_300);
        println(" /    |    \\\\___ \\  |  |  |  | \\ //__ \\_ ", PURPLE_300);
        println(" \\____|__  /____  > |__|  |__|  (____  /", PURPLE_300);
        println("         \\/     \\/                   \\/ \n", PURPLE_300);
        printVersion();
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
    public static void println(String text, AstraAnsiColors color) {
        if (ctx().isNoColor()) {
            print(text + System.lineSeparator());
        } else {
            print(ansi()
                    .fgRgb(color.getRed(), color.getGreen(), color.getBlue())
                    .a(text)
                    .reset().toString() + System.lineSeparator());
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
