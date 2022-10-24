package com.datastax.astra.cli.core;

import static com.datastax.astra.cli.core.out.AstraCliConsole.println;

import org.fusesource.jansi.Ansi;

import com.datastax.astra.cli.core.out.AstraCliConsole;
import com.datastax.astra.cli.core.out.StringBuilderAnsi;
import com.datastax.astra.cli.utils.AstraCliUtils;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * Question mark is a COMMAND from the CLI when no command name is provided
 *
 * @author Cedrick LUNVEN (@clunven)
 */
@Command(name = "?", description = "Display this help version")
public class DefaultCmd extends AbstractCmd {
    
    /** Ask for version number. s*/
    @Option(name = { "--version" }, description = "Show version")
    protected boolean version = false;
    
    /** {@inheritDoc} */
    public void execute() {
        if (version) {
            AstraCliConsole.outputData("version", AstraCliUtils.version());
        } else {
            AstraCliConsole.banner();
            println(new StringBuilderAnsi(" - Command list: ")
                    .append("astra help", Ansi.Color.CYAN));
            println(new StringBuilderAnsi("️ - Get help: ")
                    .append("astra help <your command>", Ansi.Color.CYAN));
            println("");
        }
    }    

}
