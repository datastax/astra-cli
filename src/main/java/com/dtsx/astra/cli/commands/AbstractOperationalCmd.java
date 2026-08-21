package com.dtsx.astra.cli.commands;

import com.dtsx.astra.cli.commands.user.AbstractCmd;
import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.datatypes.Thunk;
import com.dtsx.astra.cli.core.exceptions.AstraCliException;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.formats.*;
import com.dtsx.astra.cli.operations.Operation;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static com.dtsx.astra.cli.core.output.ExitCode.UNSUPPORTED_EXECUTION;

public abstract class AbstractOperationalCmd<OpRes> extends AbstractCmd {
    public static final String SHOW_CUSTOM_DEFAULT = "__show_custom_default__:";

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

    private static class UnsupportedOutputException extends RuntimeException {}

    protected OutputHuman executeHuman(Supplier<OpRes> _result) {
        throw new UnsupportedOutputException();
    }

    protected OutputJson executeJson(Supplier<OpRes> _result) {
        throw new UnsupportedOutputException();
    }

    protected OutputCsv executeCsv(Supplier<OpRes> _result) {
        throw new UnsupportedOutputException();
    }

    protected abstract Operation<OpRes> mkOperation();

    @Override
    protected final void execute() {
        val result = evokeProperExecuteFunction(ctx);

        if (!result.isEmpty()) {
            ctx.console().unsafePrintln(result.stripTrailing());
        }
    }

    private String evokeProperExecuteFunction(CliContext ctx) {
        val thunk = new Thunk<>(() -> mkOperation().execute());

        try {
            return switch (ctx.outputType()) {
                case HUMAN -> executeHuman(thunk).renderAsHuman(ctx);
                case JSON -> executeJson(thunk).renderAsJson();
                case CSV -> executeCsv(thunk).renderAsCsv();
            };
        } catch (UnsupportedOutputException e) {
            return execute(thunk).render(ctx);
        }
    }
}
