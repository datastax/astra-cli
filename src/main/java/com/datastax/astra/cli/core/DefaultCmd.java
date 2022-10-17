package com.datastax.astra.cli.core;

import static com.datastax.astra.cli.core.out.AstraCliConsole.println;

import org.fusesource.jansi.Ansi;

import com.datastax.astra.cli.core.out.AstraCliConsole;
import com.datastax.astra.cli.core.out.StringBuilderAnsi;
import com.datastax.astra.cli.utils.AstraCliUtils;
import com.github.rvesse.airline.annotations.Command;
import com.github.rvesse.airline.annotations.Option;

/**
 * The is a COMMAND from the CLI when no command name is provided
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
            println("");
            println("    _____            __                  ", Ansi.Color.GREEN);
            println("   /  _  \\   _______/  |_____________    ", Ansi.Color.GREEN);
            println("  /  /_\\  \\ /  ___/\\   __\\_  __ \\__  \\  ", Ansi.Color.GREEN);
            println(" /    |    \\\\___ \\  |  |  |  | \\// __ \\_ ", Ansi.Color.GREEN);
            println(" \\____|__  /____  > |__|  |__|  (____  /", Ansi.Color.GREEN);
            println("         \\/     \\/                   \\/ ", Ansi.Color.GREEN);
            println("");
            println(" Version: " + AstraCliUtils.version() + "\n", Ansi.Color.CYAN);
            println(new StringBuilderAnsi(" 📋 Command list: ")
                    .append("astra help", Ansi.Color.GREEN));
            println(new StringBuilderAnsi(" ℹ️ Get help: ")
                    .append("astra help <you command>", Ansi.Color.GREEN));
            println(new StringBuilderAnsi(" 🧑🏽‍💻 Get support : ")
                    .append("'https://dtsx.io/discord'", Ansi.Color.GREEN));
            println("");
        }
    }    

}
