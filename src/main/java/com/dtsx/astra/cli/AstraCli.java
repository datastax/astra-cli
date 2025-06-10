package com.dtsx.astra.cli;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.commands.config.ConfigCmd;
import com.dtsx.astra.cli.commands.db.DbCmd;
import com.dtsx.astra.cli.core.exceptions.ExecutionExceptionHandler;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputHuman;
import com.dtsx.astra.cli.core.TypeConverters;
import lombok.val;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Command;

import java.util.StringJoiner;

import static com.dtsx.astra.cli.core.output.AstraColors.*;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

@Command(
    name = "ast",
    subcommands = {
        DbCmd.class,
        ConfigCmd.class,
        AutoComplete.GenerateCompletion.class,
        CommandLine.HelpCommand.class,
    }
)
public class AstraCli extends AbstractCmd {
    public static final String VERSION = "1.0.0-alpha.0";

    public static final String BANNER = PURPLE_300.use("""
          _____            __
         /  _  \\   _______/  |_____________
        /  /_\\  \\ /  ___/\\   __\\_  __ \\__  \\
       /    |    \\\\___ \\  |  |  |  | \\ //__ \\_
       \\____|__  /____  > |__|  |__|  (____  /
               \\/     \\/                   \\/
    
                            Version: %s
    """.stripIndent().formatted(VERSION));

    @Override
    public OutputHuman executeHuman() {
        val sj = new StringJoiner(NL);

        sj.add(BANNER);

        sj.add("Documentation: " + highlight("https://awesome-astra.github.io/docs/pages/astra/astra-cli/"));
        sj.add("");
        sj.add(spec.commandLine().getUsageMessage());

        sj.add("Sample commands:");
        sj.add(" → List databases           " + highlight("astra db list"));
        sj.add(" → Create vector database   " + highlight("astra db create demo --vector"));
        sj.add(" → List collections         " + highlight("astra db list-collections demo"));

        return OutputHuman.message(sj);
    }

    public static void main(String... args) {
        val cli = new AstraCli();

        val cmd = new CommandLine(cli)
            .setColorScheme(AstraColors.DEFAULT_COLOR_SCHEME)
            .setExecutionExceptionHandler(new ExecutionExceptionHandler())
            .setOverwrittenOptionsAllowed(true);

        for (val converter : TypeConverters.INSTANCES) {
            cmd.registerConverter(converter.getClazz(), converter);
        }

        cmd.getSubcommands().get("generate-completion").getCommandSpec().usageMessage().hidden(true);
        cmd.getSubcommands().get("help").getCommandSpec().usageMessage().hidden(true);

        cmd.execute(args);
    }
}
