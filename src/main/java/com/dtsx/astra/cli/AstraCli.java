package com.dtsx.astra.cli;

import com.dtsx.astra.cli.commands.*;
import com.dtsx.astra.cli.commands.config.ConfigCmd;
import com.dtsx.astra.cli.commands.db.DbCmd;
import com.dtsx.astra.cli.commands.org.OrgCmd;
import com.dtsx.astra.cli.commands.role.RoleCmd;
import com.dtsx.astra.cli.commands.streaming.StreamingCmd;
import com.dtsx.astra.cli.commands.token.TokenCmd;
import com.dtsx.astra.cli.commands.user.UserCmd;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.TypeConverters;
import com.dtsx.astra.cli.core.config.AstraHome;
import com.dtsx.astra.cli.core.datatypes.Ref;
import com.dtsx.astra.cli.core.exceptions.ExecutionExceptionHandler;
import com.dtsx.astra.cli.core.exceptions.ExitCodeException;
import com.dtsx.astra.cli.core.exceptions.ParameterExceptionHandler;
import com.dtsx.astra.cli.core.help.DefaultsRenderer;
import com.dtsx.astra.cli.core.help.DescriptionNewlineRenderer;
import com.dtsx.astra.cli.core.help.Example;
import com.dtsx.astra.cli.core.help.ExamplesRenderer;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.AstraLogger.Level;
import com.dtsx.astra.cli.core.output.JansiUtils;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.core.properties.CliEnvironmentImpl;
import com.dtsx.astra.cli.core.properties.CliPropertiesImpl;
import com.dtsx.astra.cli.core.properties.MemoizedCliProperties;
import com.dtsx.astra.cli.core.upgrades.UpgradeNotifier;
import com.dtsx.astra.cli.gateways.GatewayProviderImpl;
import com.dtsx.astra.cli.operations.Operation;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import lombok.val;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Option;

import java.nio.file.FileSystems;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.utils.MiscUtils.mkPrintWriter;
import static com.dtsx.astra.cli.utils.StringUtils.NL;

@Command(
    name = "astra",
    descriptionHeading = " ", // normally the description heading is "%n", but we don't want that here since we have no description
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
        UpgradeCmd.class,
        NukeCmd.class,
        ShellEnvCmd.class,
        DocsCmd.class
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
    command = "${cli.name} db create demo -r us-east1"
)
public class AstraCli extends AbstractCmd<Void> {
    @Option(
        names = { "-v", "--version" },
        description = "Print version information and exit.",
        help = true
    )
    private boolean $versionRequested;

    @Override
    public final OutputHuman executeHuman(Supplier<Void> v) {
        if ($versionRequested) {
            return OutputHuman.response("v" + ctx.properties().version());
        }

        ctx.log().banner();

        val sj = new StringJoiner(NL);

        sj.add("Documentation: @!https://docs.datastax.com/en/astra-cli!@");
        sj.add("");
        sj.add(spec.commandLine().getUsageMessage());

        return OutputHuman.response(sj);
    }

    @Override
    protected OutputAll execute(Supplier<Void> v) {
        if ($versionRequested) {
            return OutputAll.response("v" + ctx.properties().version());
        }
        return super.execute(v);
    }

    @Override
    protected Operation<Void> mkOperation() {
        return () -> null;
    }

    @SneakyThrows
    public static void main(String... args) {
        val cliEnv = new CliEnvironmentImpl();

        val ctxRef = new Ref<CliContext>((getCtx) -> new CliContext(
            cliEnv,
            CliPropertiesImpl.mkAndLoadSysProps(cliEnv, MemoizedCliProperties::new),
            OutputType.HUMAN,
            new AstraColors(Ansi.AUTO),
            new AstraLogger(Level.REGULAR, getCtx, false, Optional.empty(), true),
            new AstraConsole(System.in, mkPrintWriter(System.out, "stdout"), mkPrintWriter(System.err, "stderr"), null, getCtx, false),
            new AstraHome(getCtx),
            FileSystems.getDefault(),
            new GatewayProviderImpl(),
            UpgradeNotifier::run,
            Optional.empty()
        ));

        System.exit(run(ctxRef, args));
    }

    @Getter
    @Accessors(fluent = true)
    private static @Nullable Supplier<CliContext> unsafeGlobalCliContext;

    @SneakyThrows
    @VisibleForTesting
    public static int run(Ref<CliContext> ctxRef, String... args) {
        @Cleanup val jansi = JansiUtils.installIfNecessary();

        // should only be used in dire cases where it doesn't super matter if the context is wrong,
        // and it won't affect testability.
        unsafeGlobalCliContext = ctxRef::get;

        val cli = new AstraCli();
        val cmd = new CommandLine(cli, mkFactory(ctxRef));

        // top-level command needs to be initialized manually since picocli doesn't use the factory for it
        cli.initCtx(ctxRef);

        cmd
            .setColorScheme(AstraColors.DEFAULT_COLOR_SCHEME)
            .setExecutionExceptionHandler(new ExecutionExceptionHandler(ctxRef))
            .setParameterExceptionHandler(new ParameterExceptionHandler(cmd.getParameterExceptionHandler(), ctxRef))
            .setCaseInsensitiveEnumValuesAllowed(true)
            .setOverwrittenOptionsAllowed(true);

        ctxRef.onUpdate((ctx) -> {
            cmd.setOut(ctx.console().getOut());
            cmd.setErr(ctx.console().getErr());
        });

        cmd.getSubcommands().get("help").getCommandSpec().usageMessage().hidden(true);

        for (val converter : TypeConverters.mkInstances(ctxRef)) {
            cmd.registerConverter(converter.clazz(), converter);
        }

        cmd.setHelpFactory((spec, cs) -> {
            ExamplesRenderer.installRenderer(spec.commandLine(), args, ctxRef);
            DescriptionNewlineRenderer.installRenderer(spec.commandLine());
            return DefaultsRenderer.helpWithOverriddenDefaultsRendering(spec, cs);
        });

        return cmd.execute(args);
    }

    private static IFactory mkFactory(Ref<CliContext> ctxRef) {
        return new IFactory() {
            private final IFactory defaultFactory = CommandLine.defaultFactory();

            @Override
            public <K> K create(Class<K> cls) throws Exception { // I miss having proper rank2 type support
                val created = defaultFactory.create(cls);

                if (created instanceof AbstractCmd<?> cmd) {
                    cmd.initCtx(ctxRef);
                }

                return created;
            }
        };
    }

    public static <T> T exit(int exitCode) {
        throw new ExitCodeException(exitCode);
    }
}
