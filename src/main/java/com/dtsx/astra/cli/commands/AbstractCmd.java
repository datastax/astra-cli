package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.CliEnvironment;
import com.dtsx.astra.cli.core.CliProperties;
import com.dtsx.astra.cli.core.config.AstraHome;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.AstraLogger.Level;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.*;
import com.dtsx.astra.cli.operations.Operation;
import lombok.val;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.VisibleForTesting;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.UNSUPPORTED_EXECUTION;

@Command(
    versionProvider = CliProperties.class,
    mixinStandardHelpOptions = true,
    commandListHeading = "%nCommands:%n",
    descriptionHeading = "%n",
    footer = "%nSee '${cli.name} <command> <subcommand> --help' for help on a specific subcommand."
)
public abstract class AbstractCmd<OpRes> implements Runnable {
    public static final String DEFAULT_START = "  @|faint (default: |@@|faint,italic ";
    public static final String DEFAULT_END = "|@@|faint )|@";
    public static final String DEFAULT_VALUE = DEFAULT_START + "${DEFAULT-VALUE}" + DEFAULT_END;

    @Spec
    protected CommandSpec spec;

    protected CliContext ctx;

    public AbstractCmd() {
        ctx = new CliContext(
            CliEnvironment.isWindows(),
            CliEnvironment.isTty(),
            OutputType.HUMAN,
            new AstraColors(Ansi.AUTO),
            new AstraLogger(Level.REGULAR, () -> ctx, false, Optional.empty()),
            new AstraConsole(() -> ctx, false),
            new AstraHome(),
            FileSystems.getDefault()
        );
    }

    @Mixin
    private AstraColors.Mixin csMixin;

    @Mixin
    private OutputType.Mixin outputTypeMixin;

    @Mixin
    private AstraLogger.Mixin loggerMixin;

    @Mixin
    private AstraConsole.Mixin consoleMixin;

    protected OutputAll execute(Supplier<OpRes> _result) {
        val otherTypes = Arrays.stream(OutputType.values()).filter(o -> o != ctx.outputType()).map(o -> o.name().toLowerCase()).toList();
        val otherTypesAsString = String.join("|", otherTypes);

        val originalArgsWithoutOutput = new ArrayList<>(originalArgs());

        for (val flag : List.of("-o", "--output")) {
            while (originalArgsWithoutOutput.contains(flag)) {
                int idx = originalArgsWithoutOutput.indexOf(flag);
                originalArgsWithoutOutput.remove(idx);
                originalArgsWithoutOutput.remove(idx);
            }
        }

        throw new AstraCliException(UNSUPPORTED_EXECUTION, """
          @|bold,red Error: This operation does not support outputting in the '|@@|bold,red,italic %s|@@|bold,red ' format.|@
        
          Please retry with another output format using '--output <%s>' or '-o <%s>'.
        """.formatted(
            ctx.outputType().name().toLowerCase(),
            otherTypesAsString,
            otherTypesAsString
        ), List.of(
            new Hint("Example fix", originalArgsWithoutOutput, "-o " + otherTypes.getFirst())
        ));
    }

    protected OutputHuman executeHuman(Supplier<OpRes> _result) {
        throw new UnsupportedOperationException();
    }

    protected OutputJson executeJson(Supplier<OpRes> _result) {
        throw new UnsupportedOperationException();
    }

    protected OutputCsv executeCsv(Supplier<OpRes> _result) {
        throw new UnsupportedOperationException();
    }

    protected abstract Operation<OpRes> mkOperation();

    @MustBeInvokedByOverriders
    protected void prelude() {
        spec.commandLine().setColorScheme(ctx.colorScheme());
    }

    @MustBeInvokedByOverriders
    protected void postlude() {}

    @Override
    public final void run() {
        val ansi = csMixin.ansi().orElse(
            (outputTypeMixin.requested().isHuman())
                ? Ansi.AUTO
                : Ansi.OFF
        );

        val level =
            (loggerMixin.quiet())
                ? Level.QUIET
            : (loggerMixin.verbose())
                ? Level.VERBOSE
                : Level.REGULAR;

        ctx = new CliContext(
            CliEnvironment.isWindows(),
            CliEnvironment.isTty(),
            outputTypeMixin.requested(),
            new AstraColors(ansi),
            new AstraLogger(level, () -> ctx, loggerMixin.shouldDumpLogs(), loggerMixin.dumpLogsTo()),
            new AstraConsole(() -> ctx, consoleMixin.noInput()),
            new AstraHome(),
            FileSystems.getDefault()
        );

        run(ctx);
    }

    @VisibleForTesting
    public final void run(CliContext ctx) {
        this.ctx = ctx;

        this.prelude();
        val result = evokeProperExecuteFunction(ctx);
        this.postlude();

        if (!result.isEmpty()) {
            val formatted = ctx.console().format(result.stripTrailing());
            ctx.console().getOut().println(formatted);
        }
    }

    private String evokeProperExecuteFunction(CliContext ctx) {
        val ref = new Object() {
            Optional<OpRes> cachedResult = Optional.empty();
        };

        final Supplier<OpRes> resultFn = () -> {
            if (ref.cachedResult.isEmpty()) {
                ref.cachedResult = Optional.of(mkOperation().execute());
            }
            return ref.cachedResult.get();
        };

        try {
            return switch (ctx.outputType()) {
                case HUMAN -> executeHuman(resultFn).renderAsHuman(ctx);
                case JSON -> executeJson(resultFn).renderAsJson();
                case CSV -> executeCsv(resultFn).renderAsCsv();
            };
        } catch (UnsupportedOperationException e) {
            return execute(resultFn).render(ctx);
        }
    }

    protected final List<String> originalArgs() {
        return new ArrayList<>() {{ add(CliProperties.cliName()); addAll(spec.commandLine().getParseResult().originalArgs()); }};
    }
}
