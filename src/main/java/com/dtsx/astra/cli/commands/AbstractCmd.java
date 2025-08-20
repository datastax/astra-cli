package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.CLIProperties;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.AstraColors;
import com.dtsx.astra.cli.core.output.AstraConsole;
import com.dtsx.astra.cli.core.output.AstraLogger;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.*;
import com.dtsx.astra.cli.operations.Operation;
import lombok.val;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.UNSUPPORTED_EXECUTION;
import static picocli.CommandLine.Help.Ansi.OFF;

@Command(
    versionProvider = CLIProperties.class,
    mixinStandardHelpOptions = true,
    commandListHeading = "%nCommands:%n",
    descriptionHeading = "%n",
    footer = "%nSee 'astra <command> <subcommand> --help' for help on a specific subcommand."
)
public abstract class AbstractCmd<OpRes> implements Runnable {
    public static final String DEFAULT_VALUE = "  @|faint (default: |@@|faint,italic ${DEFAULT-VALUE}|@@|faint )|@";

    @Spec
    protected CommandSpec spec;

    @Mixin
    private AstraColors.Mixin csMixin;

    @Mixin
    private OutputType.Mixin outputTypeMixin;

    @Mixin
    private AstraLogger.Mixin loggerMixin;

    protected OutputAll execute(Supplier<OpRes> _result) {
        val otherTypes = Arrays.stream(OutputType.values()).filter(o -> o != OutputType.requested()).map(o -> o.name().toLowerCase()).toList();
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
            OutputType.requested().name().toLowerCase(),
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
        spec.commandLine().setColorScheme(csMixin.getColorScheme());
    }

    @MustBeInvokedByOverriders
    protected void postlude() {}

    @Override
    public final void run() {
        if (OutputType.isNotHuman()) {
            AstraColors.ansi(OFF);
        }

        this.prelude();
        val result = evokeProperExecuteFunction();
        this.postlude();

        if (!result.isEmpty()) {
            val formatted = AstraConsole.format(result.stripTrailing());
            AstraConsole.getOut().println(formatted);
        }
    }

    private String evokeProperExecuteFunction() {
        var ref = new Object() {
            Optional<OpRes> cachedResult = Optional.empty();
        };

        final Supplier<OpRes> resultFn = () -> {
            if (ref.cachedResult.isEmpty()) {
                ref.cachedResult = Optional.of(mkOperation().execute());
            }
            return ref.cachedResult.get();
        };

        try {
            return switch (OutputType.requested()) {
                case HUMAN -> executeHuman(resultFn).renderAsHuman();
                case JSON -> executeJson(resultFn).renderAsJson();
                case CSV -> executeCsv(resultFn).renderAsCsv();
            };
        } catch (UnsupportedOperationException e) {
            return execute(resultFn).render(OutputType.requested());
        }
    }

    protected final List<String> originalArgs() {
        return new ArrayList<>() {{ add("astra"); addAll(spec.commandLine().getParseResult().originalArgs()); }};
    }
}
