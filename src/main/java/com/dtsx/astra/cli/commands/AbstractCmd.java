package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.CliProperties;
import com.dtsx.astra.cli.core.datatypes.Ref;
import com.dtsx.astra.cli.core.datatypes.Thunk;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.mixins.HelpMixin;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.UNSUPPORTED_EXECUTION;

@Command(
    commandListHeading = "%nCommands:%n",
    descriptionHeading = "%n",
    footer = "%nSee '${cli.name} <command> <subcommand> --help' for help on a specific subcommand."
)
public abstract class AbstractCmd<OpRes> implements Runnable {
    public static final String DEFAULT_START = "  @|faint (default: |@@|faint,italic ";
    public static final String DEFAULT_END = "|@@|faint )|@";
    public static final String DEFAULT_VALUE = DEFAULT_START + "${DEFAULT-VALUE}" + DEFAULT_END;

    @Mixin
    private HelpMixin helpMixin;

    @Spec
    protected CommandSpec spec;

    // in a perfect world (*cough*, kotlin) `ctx` would be a something like `val ctx get() = ctxRef.get()`
    // so that their lifecycle is tied together, but alas, we are in Java land, and I'd rather do `ctx` than `ctx()` here
    //
    // (actually, in an even more perfect world (*cough*, haskell) context could just be inherently lazy, but I digress)
    protected CliContext ctx;
    private Ref<CliContext> ctxRef;

    public void initCtx(Ref<CliContext> ctxRef) {
        this.ctxRef = ctxRef;
        this.ctx = ctxRef.get();
        ctxRef.onUpdate((ctx) -> this.ctx = ctx);
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
        
          No operation was executed; no changes were made.
        
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

    @Override
    public final void run() {
        if (ctx == null) {
            throw new CongratsYouFoundABugException("initCtx(...) was not called before run()");
        }

        val ansi = csMixin.ansi().orElse(
            (outputTypeMixin.requested().isHuman())
                ? ctx.colors().ansi()
                : Ansi.OFF
        );

        val level =
            (loggerMixin.quiet())
                ? Level.QUIET :
            (loggerMixin.verbose())
                ? Level.VERBOSE
                : ctx.logLevel();

        run(new CliContext(
            ctx.platform(),
            ctx.isTty(),
            outputTypeMixin.requested(),
            new AstraColors(ansi),
            new AstraLogger(level, () -> ctx, loggerMixin.shouldDumpLogs(), loggerMixin.dumpLogsTo(), loggerMixin.enableSpinner()),
            new AstraConsole(ctx.console().getIn(), ctx.console().getOut(), ctx.console().getErr(), ctx.console().getReadLineImpl(), () -> ctx, consoleMixin.noInput()),
            ctx.home(),
            ctx.fs(),
            ctx.gateways(),
            ctx.upgradeNotifier(),
            ctx.forceUseProfile()
        ));
    }

    @VisibleForTesting
    public final void run(CliContext ctx) {
        ctxRef.modify((_) -> ctx);
        this.prelude();
        val result = evokeProperExecuteFunction(ctx);
        this.postlude(result);
    }

    @MustBeInvokedByOverriders
    protected void prelude() {
        if (!disableUpgradeNotifier()) {
            ctx.upgradeNotifier().accept(ctx);
        }
        spec.commandLine().setColorScheme(ctx.colorScheme());
    }

    @MustBeInvokedByOverriders
    protected void postlude(String result) {
        if (!result.isEmpty()) {
            ctx.console().unsafePrintln(result.stripTrailing());
        }

        if (ctx.log().shouldDumpLogs()) {
            ctx.log().dumpLogsToFile();
        }
    }

    protected boolean disableUpgradeNotifier() {
        return false;
    }

    private String evokeProperExecuteFunction(CliContext ctx) {
        val thunk = new Thunk<>(() -> mkOperation().execute());

        try {
            return switch (ctx.outputType()) {
                case HUMAN -> executeHuman(thunk).renderAsHuman(ctx);
                case JSON -> executeJson(thunk).renderAsJson();
                case CSV -> executeCsv(thunk).renderAsCsv();
            };
        } catch (UnsupportedOperationException e) {
            return execute(thunk).render(ctx);
        }
    }

    protected final List<String> originalArgs() {
        return new ArrayList<>() {{ add(CliProperties.cliName()); addAll(spec.commandLine().getParseResult().originalArgs()); }};
    }
}
