package com.dtsx.astra.cli.commands.user;

import com.dtsx.astra.cli.AstraCli;
import com.dtsx.astra.cli.commands.AbstractOperationalCmd;
import com.dtsx.astra.cli.commands.CommonOptions;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Ref;
import com.dtsx.astra.cli.core.exceptions.internal.cli.CongratsYouFoundABugException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.AstraLogger.Level;
import lombok.val;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.util.Collections;
import java.util.List;

import static com.dtsx.astra.cli.utils.CollectionUtils.listAdd;

@Command(
    commandListHeading = "%nCommands:%n",
    descriptionHeading = "%n",
    footer = "%nSee '${cli.name} <command> <subcommand> --help' for help on a specific subcommand."
)
public abstract class AbstractCmd implements Runnable {
    @ArgGroup(validate = false, heading = "%nCommon Options:%n", order = 99)
    public CommonOptions common = CommonOptions.EMPTY;

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

    @Override
    @MustBeInvokedByOverriders
    public final void run() {
        if (ctx == null) {
            throw new CongratsYouFoundABugException("initCtx(...) was not called before run()");
        }

        val common = mergeCommonOptions();

        val ansi = common.ansi().orElse(
            (common.outputType().isHuman())
                ? ctx.colors().ansi()
                : Ansi.OFF
        );

        val level =
            (common.verbose())
                ? Level.VERBOSE :
            (common.quiet())
                ? Level.QUIET
                : ctx.logLevel();

        ctxRef.modify((_) -> new CliContext(
            ctx.env(),
            ctx.properties(),
            common.outputType(),
            new AstraColors(ansi),
            new AstraLogger(level, ctx.env(), () -> ctx, common.shouldDumpLogs(), common.dumpLogsTo(), common.enableSpinner()),
            new AstraConsole(ctx.console().stdin(), ctx.console().stdout(), ctx.console().stderr(), ctx.console().readLineImpl(), () -> ctx, common.noInput()),
            ctx.home(),
            ctx.fs(),
            ctx.gateways(),
            ctx.upgradeNotifier(),
            ctx.forceProfileForTesting()
        ));

        prelude();
        execute();
        postlude();
    }

    private CommonOptions mergeCommonOptions() {
        var common = this.common;

        for (var spec = this.spec.parent(); spec != null; spec = spec.parent()) {
            if (spec.userObject() instanceof AbstractOperationalCmd<?> cmd) {
                common = common.merge(cmd.common);
            }
        }

        return common;
    }

    @MustBeInvokedByOverriders
    protected void prelude() {
        spec.commandLine().setColorScheme(ctx.colors().colorScheme());

        if (!disableUpgradeNotifier()) {
            ctx.upgradeNotifier().accept(ctx);
        }

        if (!disableDuplicateFilesCheck()) {
            ctx.properties().detectDuplicateFileLocations(ctx);
        }

        if (common.helpRequested()) {
            spec.commandLine().usage(ctx.console().stdout());
            AstraCli.exit(0);
        }
    }

    protected void execute() {
        // noop for now
    }

    @MustBeInvokedByOverriders
    protected void postlude() {
        if (ctx.log().shouldDumpLogs()) {
            ctx.log().dumpLogsToFile();
        }
    }

    protected boolean disableUpgradeNotifier() {
        return false;
    }

    protected boolean disableDuplicateFilesCheck() {
        return false;
    }

    protected final List<String> originalArgs() {
        if (spec == null) {
            return Collections.emptyList(); // Only triggered in tests where spec is not initialized
        }

        return listAdd(ctx.properties().cliName(), spec.commandLine().getParseResult().originalArgs()).stream()
            .map(s -> s.contains(" ") ? "'" + s + "'" : s)
            .toList();
    }
}
