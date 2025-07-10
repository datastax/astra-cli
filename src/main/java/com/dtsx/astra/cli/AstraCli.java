package com.dtsx.astra.cli;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.commands.CompletionsCmd;
import com.dtsx.astra.cli.commands.SetupCmd;
import com.dtsx.astra.cli.commands.config.ConfigCmd;
import com.dtsx.astra.cli.commands.db.DbCmd;
import com.dtsx.astra.cli.commands.org.OrgCmd;
import com.dtsx.astra.cli.commands.role.RoleCmd;
import com.dtsx.astra.cli.commands.streaming.StreamingCmd;
import com.dtsx.astra.cli.commands.token.TokenCmd;
import com.dtsx.astra.cli.commands.user.UserCmd;
import com.dtsx.astra.cli.core.TypeConverters;
import com.dtsx.astra.cli.core.exceptions.ExecutionExceptionHandler;
import com.dtsx.astra.cli.core.exceptions.ParameterExceptionHandler;
import com.dtsx.astra.cli.core.help.DescriptionNewlineRenderer;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.help.ExamplesRenderer;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.output.OutputHuman;
import com.dtsx.astra.cli.operations.Operation;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.jansi.graalvm.AnsiConsole;

import java.util.StringJoiner;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

@Command(
    name = "ast",
    subcommands = {
        CommandLine.HelpCommand.class,
        SetupCmd.class,
        ConfigCmd.class,
        DbCmd.class,
        OrgCmd.class,
        RoleCmd.class,
        StreamingCmd.class,
        TokenCmd.class,
        UserCmd.class,
        CompletionsCmd.class,
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
    command = "astra db create demo -r us-east1 --vector"
)
public class AstraCli extends AbstractCmd<Void> {
    @Override
    public OutputHuman executeHuman(Void v) {
        AstraLogger.banner();

        val sj = new StringJoiner(NL);

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
        {
            final String CSI = "\u001b[";
            String blue300 = CSI + "38;2;129;163;231m";
            String reset = CSI + "0m";

            System.out.println(blue300 + "I am so bloody confused" + reset);
            System.out.println(highlight("I am so bloody confused"));

            for (val color : AstraColors.values()) {
                System.out.println(color.use(color.name()));
            }
        }

        try {
            AnsiConsole.systemInstall();
            val cmd = new CommandLine(new AstraCli());


            final String CSI = "\u001b[";
            String blue300 = CSI + "38;2;129;163;231m";
            String reset = CSI + "0m";

            System.out.println(blue300 + "I am so bloody confused" + reset);
            System.out.println(highlight("I am so bloody confused"));

            for (val color : AstraColors.values()) {
                System.out.println(color.use(color.name()));
            }


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
        } finally {
            AnsiConsole.systemUninstall();
        }
    }

    public static <T> T exit(int exitCode) {
        System.exit(exitCode);
        return null;
    }
}
