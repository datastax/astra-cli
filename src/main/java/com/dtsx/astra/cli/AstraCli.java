package com.dtsx.astra.cli;

import com.dtsx.astra.cli.commands.AbstractCmd;
import com.dtsx.astra.cli.commands.CompletionsCmd;
import com.dtsx.astra.cli.commands.NukeCmd;
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
import com.dtsx.astra.cli.core.output.JansiUtils;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.operations.Operation;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;

import java.util.StringJoiner;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.AstraColors.highlight;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

@Command(
    name = "${cli.name}",
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
        NukeCmd.class,
    }
)
@Example(
    comment = "Setup the Astra CLI",
    command = "${cli.name} setup"
)
@Example(
    comment = "List databases",
    command = "${cli.name} db list"
)
@Example(
    comment = "Create a vector database",
    command = "${cli.name} db create demo -r us-east1 --vector"
)
public class AstraCli extends AbstractCmd<Void> {
    @Override
    public OutputHuman executeHuman(Supplier<Void> v) {
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

    @SneakyThrows
    public static void main(String... args) {
        @Cleanup val jansi = JansiUtils.installIfNecessary();
        val cmd = new CommandLine(new AstraCli());

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

        cmd.execute(args);
    }

    public static <T> T exit(int exitCode) {
        System.exit(exitCode);
        return null;
    }
}
