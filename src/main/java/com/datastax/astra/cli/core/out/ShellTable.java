package com.datastax.astra.cli.core.out;

import com.datastax.astra.cli.core.CliContext;
import com.datastax.astra.cli.core.ExitCode;
import org.fusesource.jansi.Ansi;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Standardize output for tables.
 *
 * @author Cedrick LUNVEN (@clunven)
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
    private Ansi.Color tableColor = Ansi.Color.CYAN;
    
    /**
     * Color of title
     */
    private Ansi.Color  columnTitlesColor = Ansi.Color.YELLOW;

    /**
     * Color of cell
     */
    private Ansi.Color cellColor = Ansi.Color.WHITE;

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
        setColumnTitlesColor(Ansi.Color.YELLOW);
        setCellColor(Ansi.Color.WHITE);
        setTableColor(Ansi.Color.CYAN);
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
        
        StringBuilderAnsi builder = new StringBuilderAnsi();
        computeColumnsWidths();
        
        // Compute Table Horizontal Line
        String tableLine = buildTableLines();
        
        // Header
        builder.append(tableLine + "+\n", tableColor);
        buildTableHeader(builder);
        builder.append(tableLine + "+\n", tableColor);
        
        // Display Data
        buildTableData(builder);
        builder.append(tableLine + "+\n", tableColor);
        AstraCliConsole.println(builder);
    }
    
    /**
     * Display Column Titles
     */
    private void buildTableData(StringBuilderAnsi builder) {
        for (Map<String, String > res : cellValues) {
            // Keep Orders
            for(String columnName : columnTitlesNames) {
                builder.append("| ", tableColor);
                builder.append(res.get(columnName), cellColor, columnSize.get(columnName));
            }
            builder.append("|\n", tableColor);
        }
    }
    
    /**
     * Display Column Titles
     */
    private void buildTableHeader(StringBuilderAnsi builder) {
        for(String columnName : columnTitlesNames) {
            builder.append("| ", tableColor);
            Integer size = columnSize.get(columnName);
            if (null == size) {
                size = columnName.length() + 1;
            }
            builder.append(columnName , columnTitlesColor, size);
        }
        builder.append("|\n", tableColor);
    }
    
    /**
     * Build column width.
     */
    private void computeColumnsWidths() {
        cellValues.forEach(myRow -> columnTitlesNames.forEach(colName -> {
            if (!columnSize.containsKey(colName) ||
                 columnSize.get(colName) <  Math.max(colName.length(), myRow.get(colName).length())) {
                columnSize.put(colName,  Math.max(colName.length(), myRow.get(colName).length()) + 1);
            }
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
            Integer size = columnSize.get(columnName);
            if (null == size) {
                size = columnName.length() + 1;
            }
            tableLine.append("+").append(String.format("%-" + (size + 1) + "s", "-").replace(" ", "-"));
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
     * Setter accessor for attribute 'tableColor'.
     * @param tableColor
     * 		new value for 'tableColor '
     */
    public void setTableColor(Ansi.Color tableColor) {
        this.tableColor = tableColor;
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
     * Setter accessor for attribute 'columnTitlesColor'.
     * @param columnTitlesColor
     * 		new value for 'columnTitlesColor '
     */
    public void setColumnTitlesColor(Ansi.Color columnTitlesColor) {
        this.columnTitlesColor = columnTitlesColor;
    }

    /**
     * Setter accessor for attribute 'cellColor'.
     * @param cellColor
     * 		new value for 'cellColor '
     */
    public void setCellColor(Ansi.Color cellColor) {
        this.cellColor = cellColor;
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
