package com.dtsx.astra.cli;

import com.dtsx.astra.cli.AstraCli.SetupExampleProvider;
import com.dtsx.astra.cli.commands.*;
import com.dtsx.astra.cli.commands.config.ConfigCmd;
import com.dtsx.astra.cli.commands.db.DbCmd;
import com.dtsx.astra.cli.commands.org.OrgCmd;
import com.dtsx.astra.cli.commands.pcu.PcuCmd;
import com.dtsx.astra.cli.commands.role.RoleCmd;
import com.dtsx.astra.cli.commands.streaming.StreamingCmd;
import com.dtsx.astra.cli.commands.token.TokenCmd;
import com.dtsx.astra.cli.commands.user.UserCmd;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.TypeConverters;
import com.dtsx.astra.cli.core.config.AstraConfig;
import com.dtsx.astra.cli.core.config.AstraHome;
import com.dtsx.astra.cli.core.datatypes.Ref;
import com.dtsx.astra.cli.core.docs.AliasForSubcommand;
import com.dtsx.astra.cli.core.docs.AliasForSubcommand.None;
import com.dtsx.astra.cli.core.exceptions.ExecutionExceptionHandler;
import com.dtsx.astra.cli.core.exceptions.ExitCodeException;
import com.dtsx.astra.cli.core.exceptions.ParameterExceptionHandler;
import com.dtsx.astra.cli.core.help.*;
import com.dtsx.astra.cli.core.help.Example.ExampleProvider;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.AstraLogger.Level;
import com.dtsx.astra.cli.core.output.JansiUtils;
import com.dtsx.astra.cli.core.output.formats.OutputAll;
import com.dtsx.astra.cli.core.output.formats.OutputHuman;
import com.dtsx.astra.cli.core.output.formats.OutputType;
import com.dtsx.astra.cli.core.properties.CliEnvironmentImpl;
import com.dtsx.astra.cli.core.properties.CliProperties.ConstEnvVars;
import com.dtsx.astra.cli.core.properties.CliPropertiesImpl;
import com.dtsx.astra.cli.core.properties.MemoizedCliProperties;
import com.dtsx.astra.cli.core.upgrades.UpgradeNotifier;
import com.dtsx.astra.cli.gateways.GatewayProviderImpl;
import com.dtsx.astra.cli.operations.Operation;
import lombok.Cleanup;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.IFactory;
import picocli.CommandLine.Option;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.sql.DataTruncation;
import java.util.Arrays;
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
        PcuCmd.class,
        OrgCmd.class,
        RoleCmd.class,
        StreamingCmd.class,
        TokenCmd.class,
        UserCmd.class,
        CompletionsCmd.class,
        UpgradeCmd.class,
        ShellEnvCmd.class,
        NukeCmd.class,
        DocsCmd.class
    }
)
@Example(
    exampleProvider = SetupExampleProvider.class
)
@Example(
    comment = "List databases",
    command = "${cli.name} db list"
)
@Example(
    comment = "Create a vector database",
    command = "${cli.name} db create demo -r us-east1"
)
@AliasForSubcommand(None.class)
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
            new AstraLogger(Level.REGULAR, cliEnv, getCtx, false, Optional.empty(), Optional.empty()),
            new AstraConsole(System.in, mkPrintWriter(System.out, "stdout"), mkPrintWriter(System.err, "stderr"), null, getCtx, false),
            new AstraHome(getCtx),
            FileSystems.getDefault(),
            new GatewayProviderImpl(getCtx),
            UpgradeNotifier::run,
            Optional.empty()
        ));

        System.exit(run(ctxRef, args));
    }

    @Getter
    private static @Nullable Supplier<CliContext> unsafeGlobalCliContext;

    @SneakyThrows
    @VisibleForTesting
    @SuppressWarnings("unused")
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

        ctxRef.nowAndOnUpdate((ctx) -> {
            cmd.setOut(ctx.console().stdout());
            cmd.setErr(ctx.console().stderr());
        });

        cmd.getSubcommands().get("help").getCommandSpec().usageMessage().hidden(true);

        for (val converter : TypeConverters.mkInstances(ctxRef)) {
            cmd.registerConverter(converter.clazz(), converter);
        }

        cmd.setHelpFactory((spec, cs) -> {
            ExamplesRenderer.installRenderer(spec.commandLine(), args, ctxRef);
            DescriptionNewlineRenderer.installRenderer(spec.commandLine());
            FixedDescriptionRenderer.installRenderer(spec.commandLine());
            return DefaultsRenderer.helpWithOverriddenDefaultsRendering(spec, cs);
        });

        val allArgs = ArrayUtils.addAll(defaultArgs(), args);

        return cmd.execute(allArgs);
    }

    private static String[] defaultArgs() {
        val defaultArgsStr = Optional.ofNullable(System.getenv(ConstEnvVars.DEFAULT_ARGS))
            .orElse("");

        return Arrays.stream(defaultArgsStr.split("\\s+"))
            .filter(s -> !s.isBlank())
            .toArray(String[]::new);
    }

    private static IFactory mkFactory(Ref<CliContext> ctxRef) {
        return new IFactory() {
            private final IFactory defaultFactory = CommandLine.defaultFactory();

            @Override
            public <K> K create(Class<K> cls) throws Exception { // I miss having proper rank2 type support for lambdas
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

    public static class SetupExampleProvider implements ExampleProvider {
        @Override
        public Pair<String, String[]> get(CliContext ctx) {
            if (System.getProperty("cli.testing") == null) { // keeps output deterministic for testing
                try {
                    val configFileExists = Files.exists(AstraConfig.resolveDefaultAstraConfigFile(ctx));

                    if (!configFileExists) {
                        return Pair.of("Setup the Astra CLI", new String[] { "${cli.name} setup" });
                    }

                    val autocompleteSetup = System.getenv(ConstEnvVars.COMPLETIONS_SETUP) != null;

                    if (!autocompleteSetup && ctx.isNotWindows()) {
                        return Pair.of("Put this in your shell profile to generate completions and more!", new String[] { "eval \"$(${cli.path} shellenv)\"" });
                    }
                } catch (Exception e) {
                    ctx.log().exception("Error resolving main example for AstraCli", e);
                }
            }

            return Pair.of("Create a new profile", new String[] { "${cli.name} setup"});
        }
    }
}
