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
import org.apache.commons.lang3.StringUtils;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dtsx.astra.cli.core.out.AstraAnsiColors.BLUE_300;
import static com.dtsx.astra.cli.core.out.AstraAnsiColors.NEUTRAL_300;

/**
 * Standardize output for tables.
 */
public class ShellTable implements Serializable {

    /** Serial */
    @Serial
    private static final long serialVersionUID = -2134504321420499395L;

    /** Column name. */
    public static final String COLUMN_PROPERTY_NAME    = "Attribute";

    /** Column name. */
    public static final String COLUMN_PROPERTY_VALUE   = "Value";

    /**
     * Color of table.
     */
    private static final AstraAnsiColors TABLE_COLOR = BLUE_300;

    /**
     * Color of title
     */
    private static final AstraAnsiColors COLUMN_TITLES_COLOR = BLUE_300;

    /**
     * Color of cell
     */
    private static final AstraAnsiColors CELL_COLOR = NEUTRAL_300;

    /**
     * Title column names
     */
    private final List < String > columnTitlesNames = new ArrayList<>();

    /**
     * Columns sizes
     */
    private final Map < String, Integer > columnSize = new HashMap<>();

    /**
     * Cell values
     */
    private final List< Map < String, String > > cellValues = new ArrayList<>();

    /** Shell Table */
    public ShellTable() {
        /*
         * Static initialization for property required a default constructor.
         */
    }

    /**
     * Create a property table
     * 
     * @param widthName
     *      name
     * @param widthValue
     *      value
     * @return
     *      table initialized
     */
    public static ShellTable propertyTable(int widthName, int widthValue) {
        ShellTable sht = new ShellTable();
        sht.addColumn(COLUMN_PROPERTY_NAME, widthName);
        sht.addColumn(COLUMN_PROPERTY_VALUE, widthValue);
        return sht;
    }
    
    /**
     * Show as Json.
     */
    public void showJson() {
        AstraCliConsole.printJson(
                new JsonOutput<>(ExitCode.SUCCESS,
                        ctx().getArguments().toString(),
                        getCellValues()));
    }
    
    
    private CliContext ctx() {
        return CliContext.getInstance();
    }
    
    /**
     * Show as Csv
     */
    public void showCsv() {
        AstraCliConsole.printCsv(new CsvOutput(getColumnTitlesNames(), getCellValues()));
    }
    
    /**
     * Display the table in the shell.
     */
    public void show() {

        if (!ctx().isNoColor()) {
            StringBuilderAnsi builder = new StringBuilderAnsi();
            computeColumnsWidths();
            String tableLine = buildTableLines();

            // Header
            builder.append(tableLine + "+\n", TABLE_COLOR);
            buildTableHeader(builder);
            builder.append(tableLine + "+\n", TABLE_COLOR);

            // Display Data
            buildTableData(builder);
            builder.append(tableLine + "+\n", TABLE_COLOR);
            AstraCliConsole.println(builder);
        } else {
            StringBuilder builder = new StringBuilder();
            computeColumnsWidths();
            String tableLine = buildTableLines();
            builder.append(tableLine).append("+\n");
            buildTableHeaderNoColor(builder);
            builder.append(tableLine).append("+\n");
            buildTableDataNoColor(builder);
            builder.append(tableLine).append("+\n");
            AstraCliConsole.println(builder.toString());
        }

    }
    
    /**
     * Display Column Titles
     */
    private void buildTableData(StringBuilderAnsi builder) {
        for (Map<String, String > res : cellValues) {
            // Keep Orders
            for(String columnName : columnTitlesNames) {
                builder.append("| ", TABLE_COLOR);
                // Handle color
                builder.append(res.get(columnName), CELL_COLOR, columnSize.get(columnName));
            }
            builder.append("|\n", TABLE_COLOR);
        }
    }

    /**
     * Display Column Titles
     */
    private void buildTableDataNoColor(StringBuilder builder) {
        for (Map<String, String > res : cellValues) {
            // Keep Orders
            for(String columnName : columnTitlesNames) {
                builder.append("| ");
                // Handle color
                builder.append(StringUtils.rightPad(res.get(columnName), columnSize.get(columnName)));
            }
            builder.append("|\n");
        }
    }
    
    /**
     * Display Column Titles
     */
    private void buildTableHeader(StringBuilderAnsi builder) {
        for(String columnName : columnTitlesNames) {
            builder.append("| ", TABLE_COLOR);
            Integer size = columnSize.get(columnName);
            if (null == size) {
                size = columnName.length() + 1;
            }
            builder.append(columnName , COLUMN_TITLES_COLOR, size);
        }
        builder.append("|\n", TABLE_COLOR);
    }

    /**
     * Display Column Titles
     */
    private void buildTableHeaderNoColor(StringBuilder builder) {
        for(String columnName : columnTitlesNames) {
            builder.append("| ");
            Integer size = columnSize.get(columnName);
            if (null == size) {
                size = columnName.length() + 1;
            }
            builder.append(StringUtils.rightPad(columnName, size));
        }
        builder.append("|\n");
    }


    
    /**
     * Build column width.
     */
    private void computeColumnsWidths() {
        cellValues.forEach(myRow -> columnTitlesNames.forEach(colName -> {
            String uncoloredText = myRow.get(colName).replaceAll("\u001B\\[[;\\d]*m", "");
            int max = Math.max(colName.length(), uncoloredText.length());
            if (!columnSize.containsKey(colName) || columnSize.get(colName) < max) columnSize.put(colName, max + 1);
        }));
    }
    
    /**
     * Build table lines.
     * 
     * @return
     *      line
     */
    private String buildTableLines() {
        StringBuilder tableLine = new StringBuilder();
        for(String columnName : columnTitlesNames) {
            int size = columnSize.get(columnName) + 1;
            String formatPattern = "%-"+(size)+"s";
            tableLine.append("+")
                    .append(String.format(formatPattern, "-")
                    .replace(" ", "-"));
        }
        return tableLine.toString();
    }
    
    /**
     * Add a property in a table.
     * 
     * @param name
     *      key name
     * @param value
     *      key value
     */
    public void addPropertyRow(String name, String value) {
        getCellValues().add(buildProperty(name, value));
    }
    
    /**
     * Show a list in the table.
     *
     * @param name
     *      attribute names
     * @param values
     *      items of the list
     */
    public void addPropertyListRows(String name, List<String> values) {
        if (values != null && !values.isEmpty()) {
            addPropertyRow(" ", "  ");
            int idx = 0;
            for(String rsc: values) {
                if (idx == 0) {
                    addPropertyRow(name, "[" + idx + "] " + rsc);
                } else {
                    addPropertyRow(" ",  "[" + idx + "] " + rsc);
                }
                idx++;
            }
            addPropertyRow(" ", "  ");
        }
    }
    
    /**
     * Add property in a table.
     *
     * @param name
     *      property name
     * @param value
     *      property value
     * @return
     *      new row
     */
    public static Map<String, String > buildProperty(String name, String value) {
        Map <String, String> rf = new HashMap<>();
        rf.put(COLUMN_PROPERTY_NAME, name);
        rf.put(COLUMN_PROPERTY_VALUE, value);
        return rf;
    }
    
    /**
     * Add a column.
     *
     * @param columnName
     *      name
     * @param columnWidth
     *      with
     */
    public void addColumn(String columnName, int columnWidth) {
        getColumnTitlesNames().add(columnName);
        getColumnSize().put(columnName, columnWidth);
    }

    /**
     * Getter accessor for attribute 'columnTitlesNames'.
     *
     * @return
     *       current value of 'columnTitlesNames'
     */
    public List<String> getColumnTitlesNames() {
        return columnTitlesNames;
    }

    /**
     * Getter accessor for attribute 'cellValues'.
     *
     * @return
     *       current value of 'cellValues'
     */
    public List<Map<String, String>> getCellValues() {
        return cellValues;
    }

    /**
     * Getter accessor for attribute 'columnSize'.
     *
     * @return
     *       current value of 'columnSize'
     */
    public Map<String, Integer> getColumnSize() {
        return columnSize;
    }

}
