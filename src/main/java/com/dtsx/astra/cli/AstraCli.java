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
import com.dtsx.astra.cli.core.datatypes.Ref;
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
import com.dtsx.astra.cli.gateways.GatewayProviderImpl;
import com.dtsx.astra.cli.operations.Operation;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.val;
import org.jetbrains.annotations.VisibleForTesting;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.IFactory;

import java.nio.file.FileSystems;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.utils.MiscUtils.mkPrintWriter;
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
        val ctxRef = new Ref<CliContext>((getCtx) -> new CliContext(
            CliEnvironment.unsafeIsWindows(),
            CliEnvironment.unsafeIsTty(),
            OutputType.HUMAN,
            new AstraColors(Ansi.AUTO),
            new AstraLogger(Level.REGULAR, getCtx, false, Optional.empty()),
            new AstraConsole(System.in, mkPrintWriter(System.out, "stdout"), mkPrintWriter(System.err, "stderr"), getCtx, false),
            new AstraHome(FileSystems.getDefault(), CliEnvironment.unsafeIsWindows()),
            FileSystems.getDefault(),
            new GatewayProviderImpl()
        ));

        exit(run(ctxRef, args));
    }

    @Getter
    @Accessors(fluent = true)
    private static Supplier<CliContext> unsafeGlobalCliContext;

    @SneakyThrows
    @VisibleForTesting
    public static int run(Ref<CliContext> ctxRef, String... args) {
        @Cleanup val jansi = JansiUtils.installIfNecessary();

        val cli = new AstraCli();
        val cmd = new CommandLine(cli, mkFactory(ctxRef));

        // should only be used in dire cases where it doesn't super matter if the context is wrong,
        // and it won't affect testability.
        unsafeGlobalCliContext = ctxRef::unwrap;

        cmd
            .setColorScheme(AstraColors.DEFAULT_COLOR_SCHEME)
            .setExecutionExceptionHandler(new ExecutionExceptionHandler(unsafeGlobalCliContext))
            .setParameterExceptionHandler(new ParameterExceptionHandler(cmd.getParameterExceptionHandler(), unsafeGlobalCliContext))
            .setCaseInsensitiveEnumValuesAllowed(true)
            .setOverwrittenOptionsAllowed(true);

        ctxRef.onUpdate((ctx) -> {
            cmd.setOut(ctx.console().getOut());
            cmd.setErr(ctx.console().getErr());
        });

        cmd.getSubcommands().get("help").getCommandSpec().usageMessage().hidden(true);

        for (val converter : TypeConverters.INSTANCES) {
            cmd.registerConverter(converter.clazz(), converter);
        }

        cmd.setHelpFactory((spec, cs) -> {
            ExamplesRenderer.installRenderer(spec.commandLine(), args);
            DescriptionNewlineRenderer.installRenderer(spec.commandLine());
            return new Help(spec, cs);
        });

        return cmd.execute(args);
    }

    private static IFactory mkFactory(Ref<CliContext> ctxRef) {
        return new IFactory() {
            private final IFactory defaultFactory = CommandLine.defaultFactory();

            @Override
            public <K> K create(Class<K> cls) throws Exception {
                val created = defaultFactory.create(cls);

                if (created instanceof AbstractCmd<?> acmd) {
                    acmd.initCtx(ctxRef);
                }

                return created;
            }
        };
    }

    public static <T> T exit(int exitCode) {
        System.exit(exitCode);
        return null;
    }
}
