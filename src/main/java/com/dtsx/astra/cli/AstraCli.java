package com.dtsx.astra.cli;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.commands.config.ConfigCmd;
import com.dtsx.astra.cli.commands.db.DbCmd;
import com.dtsx.astra.cli.output.AstraColors;
import com.dtsx.astra.cli.utils.TypeConverters;
import com.dtsx.astra.sdk.utils.AstraEnvironment;
import lombok.val;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.*;

import java.util.StringJoiner;

import static com.dtsx.astra.cli.output.AstraColors.BLUE_300;
import static com.dtsx.astra.cli.output.AstraColors.PURPLE_300;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

@Command(
    name = "ast",
    subcommands = {
        DbCmd.class,
        ConfigCmd.class,
        AutoComplete.GenerateCompletion.class,
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
    public String executeHuman() {
        val sj = new StringJoiner(NL);

        sj.add(BANNER);

        sj.add("Documentation: " + BLUE_300.use("https://awesome-astra.github.io/docs/pages/astra/astra-cli/"));
        sj.add("");
        sj.add(spec.commandLine().getUsageMessage());

        sj.add("Sample commands:");
        sj.add(" → List databases           " + BLUE_300.use("astra db list"));
        sj.add(" → Create vector database   " + BLUE_300.use("astra db create demo --vector"));
        sj.add(" → List collections         " + BLUE_300.use("astra db list-collections demo"));

        return sj.toString();
    }

    public static void main(String... args) {
        val cli = new AstraCli();

        val cmd = new CommandLine(cli)
            .setColorScheme(AstraColors.DEFAULT_COLOR_SCHEME)
            .registerConverter(AstraEnvironment.class, new TypeConverters.ToAstraEnvironment())
            .setOverwrittenOptionsAllowed(true);

        cmd.getSubcommands().get("generate-completion").getCommandSpec().usageMessage().hidden(true);

        cmd.execute(args);
    }
}
