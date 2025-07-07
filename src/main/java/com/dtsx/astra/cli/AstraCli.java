package com.dtsx.astra.cli;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.commands.CompletionsCmd;
import com.dtsx.astra.cli.commands.config.ConfigCmd;
import com.dtsx.astra.cli.commands.db.DbCmd;
import com.dtsx.astra.cli.commands.org.OrgCmd;
import com.dtsx.astra.cli.commands.role.RoleCmd;
import com.dtsx.astra.cli.commands.streaming.StreamingCmd;
import com.dtsx.astra.cli.commands.token.TokenCmd;
import com.dtsx.astra.cli.commands.user.UserCmd;
import com.dtsx.astra.cli.core.exceptions.ExecutionExceptionHandler;
import com.dtsx.astra.cli.core.exceptions.ParameterExceptionHandler;
import com.dtsx.astra.cli.core.help.DescriptionNewlineRenderer;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.help.ExamplesRenderer;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.output.OutputHuman;
import com.dtsx.astra.cli.core.TypeConverters;
import com.dtsx.astra.cli.operations.Operation;
import lombok.val;
import picocli.AutoComplete;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;

import java.util.StringJoiner;

import static com.dtsx.astra.cli.core.output.AstraColors.*;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

@Command(
    name = "ast",
    subcommands = {
        DbCmd.class,
        ConfigCmd.class,
        OrgCmd.class,
        RoleCmd.class,
        StreamingCmd.class,
        TokenCmd.class,
        UserCmd.class,
        CompletionsCmd.class,
        CommandLine.HelpCommand.class,
    }
)
@Example(
    comment = "Setup the Astra CLI",
    command = "astra setup"
)
@Example(
    comment = "List databases",
    command = "astra db list"
)
@Example(
    comment = "Create a vector database",
    command = "astra db create demo --vector"
)
public class AstraCli extends AbstractCmd<Void> {
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
    public OutputHuman executeHuman(Void v) {
        val sj = new StringJoiner(NL);

        sj.add(BANNER);

        sj.add("Documentation: " + highlight("https://awesome-astra.github.io/docs/pages/astra/astra-cli/"));
        sj.add("");
        sj.add(spec.commandLine().getUsageMessage());

        return OutputHuman.message(sj);
    }

    @Override
    protected Operation<Void> mkOperation() {
        return () -> null;
    }

    public static void main(String... args) {
        AstraCli command = new AstraCli();
        val cmd = new CommandLine(command);

        cmd
            .setColorScheme(AstraColors.colorScheme())
            .setExecutionExceptionHandler(new ExecutionExceptionHandler())
            .setParameterExceptionHandler(new ParameterExceptionHandler(cmd.getParameterExceptionHandler()))
            .setCaseInsensitiveEnumValuesAllowed(true)
            .setOverwrittenOptionsAllowed(true);

        for (val converter : TypeConverters.INSTANCES) {
            cmd.registerConverter(converter.clazz(), converter);
        }

        cmd.setHelpFactory((spec, cs) -> {
            ExamplesRenderer.installRenderer(spec.commandLine());
            DescriptionNewlineRenderer.installRenderer(spec.commandLine());
            return new Help(spec, cs);
        });

        cmd.getSubcommands().get("generate-completion").getCommandSpec().usageMessage().hidden(true);

        cmd.execute(args);
    }

    public static <T> T exit(int exitCode) {
        System.exit(exitCode);
        return null;
    }
}
