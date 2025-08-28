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
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.CliEnvironment;
import com.dtsx.astra.cli.core.TypeConverters;
import com.dtsx.astra.cli.core.config.AstraHome;
import com.dtsx.astra.cli.core.exceptions.ExecutionExceptionHandler;
import com.dtsx.astra.cli.core.exceptions.ParameterExceptionHandler;
import com.dtsx.astra.cli.core.help.DescriptionNewlineRenderer;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.help.ExamplesRenderer;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.AstraLogger.Level;
import com.dtsx.astra.cli.core.output.JansiUtils;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.operations.Operation;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.val;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;

import java.nio.file.FileSystems;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Supplier;

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
    public final OutputHuman executeHuman(Supplier<Void> v) {
        ctx.log().banner();

        val sj = new StringJoiner(NL);

        sj.add("Documentation: @!https://awesome-astra.github.io/docs/pages/astra/astra-cli/!@");
        sj.add("");
        sj.add(spec.commandLine().getUsageMessage());

        return OutputHuman.response(sj);
    }

    @Override
    protected Operation<Void> mkOperation() {
        return () -> null;
    }

    @SneakyThrows
    public static void main(String... args) {
        exit(run(args));
    }

    private static Supplier<CliContext> unsafeGlobalCliContext;

    public static CliContext unsafeGlobalCliContext() {
        return unsafeGlobalCliContext.get();
    }

    @SneakyThrows
    public static int run(String... args) {
        @Cleanup val jansi = JansiUtils.installIfNecessary();

        val cli = new AstraCli();
        val cmd = new CommandLine(cli);

        var defaultCtx = new Object() {
            CliContext ref = null;
        };

        defaultCtx.ref = new CliContext(
            CliEnvironment.isWindows(),
            CliEnvironment.isTty(),
            OutputType.HUMAN,
            new AstraColors(Ansi.AUTO),
            new AstraLogger(Level.REGULAR, () -> defaultCtx.ref, false, Optional.empty()),
            new AstraConsole(() -> defaultCtx.ref, false),
            new AstraHome(),
            FileSystems.getDefault()
        );

        unsafeGlobalCliContext = () -> Optional.of(cli.ctx).orElse(defaultCtx.ref);

        cmd
            .setColorScheme(AstraColors.DEFAULT_COLOR_SCHEME)
            .setExecutionExceptionHandler(new ExecutionExceptionHandler(unsafeGlobalCliContext))
            .setParameterExceptionHandler(new ParameterExceptionHandler(cmd.getParameterExceptionHandler(), unsafeGlobalCliContext))
            .setCaseInsensitiveEnumValuesAllowed(true)
            .setOverwrittenOptionsAllowed(true);

        cmd.getSubcommands().get("help").getCommandSpec().usageMessage().hidden(true);

        for (val converter : TypeConverters.INSTANCES) {
            cmd.registerConverter(converter.clazz(), converter);
        }

        cmd.setHelpFactory((spec, cs) -> {
            ExamplesRenderer.installRenderer(spec.commandLine());
            DescriptionNewlineRenderer.installRenderer(spec.commandLine());
            return new Help(spec, cs);
        });

        val exitCode = cmd.execute(args);

        if (cli.ctx != null && cli.ctx.log().shouldDumpLogs()) {
            cli.ctx.log().dumpLogsToFile();
        }

        return exitCode;
    }

    public static <T> T exit(int exitCode) {
        System.exit(exitCode);
        return null;
    }
}
