package com.datastax.astra.cli.core;

import static com.datastax.astra.cli.core.out.AstraCliConsole.println;

import org.fusesource.jansi.Ansi;

import com.datastax.astra.cli.core.exception.InvalidTokenException;
import com.datastax.astra.cli.core.exception.TokenNotFoundException;
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
@Command(name = "version", description = "Interactive mode (default if no command provided)")
public class DefaultCmd extends AbstractCmd {
    
    /** Ask for version number. s*/
    @Option(name = { "--version" }, description = "Show version")
    protected boolean version = false;
   
    /** {@inheritDoc} */
    @Override
    public void init() 
    throws TokenNotFoundException, InvalidTokenException {
       ctx().init(this);
    }
    
    /** {@inheritDoc} */
    public void execute() {
        if (version) {
            AstraCliConsole.outputData("version", AstraCliUtils.version());
        } else {
            println("");
            println("  █████╗ ███████╗████████╗██████╗  █████╗   ", Ansi.Color.MAGENTA);
            println(" ██╔══██╗██╔════╝╚══██╔══╝██╔══██╗██╔══██╗  ", Ansi.Color.MAGENTA);
            println(" ███████║███████╗   ██║   ██████╔╝███████║  ", Ansi.Color.MAGENTA);
            println(" ██╔══██║╚════██║   ██║   ██╔══██╗██╔══██║  ", Ansi.Color.MAGENTA);
            println(" ██║  ██║███████║   ██║   ██║  ██║██║  ██║  ", Ansi.Color.MAGENTA);
            println(" ╚═╝  ╚═╝╚══════╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═╝  ", Ansi.Color.MAGENTA);
            println("");
            println(" Version: " + AstraCliUtils.version() + "\n", Ansi.Color.CYAN);
            println(new StringBuilderAnsi(" 📋 Command list: ")
                    .append("'astra help'", Ansi.Color.GREEN));
            println(new StringBuilderAnsi(" ℹ️  Command help: ")
                    .append("'astra help <cmd>'", Ansi.Color.GREEN)
                    .append(" (eg: astra help db create)"));
            println(new StringBuilderAnsi(" 🧑🏽‍💻 Get support: ")
                    .append("'https://dtsx.io/discord'", Ansi.Color.GREEN));
            println("");
        }
    }    

}
