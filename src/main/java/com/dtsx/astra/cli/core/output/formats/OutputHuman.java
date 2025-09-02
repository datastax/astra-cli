package com.dtsx.astra.cli.core.output.formats;

import com.dtsx.astra.cli.core.CliContext;
import com.dtsx.astra.cli.core.output.Hint;
import com.dtsx.astra.cli.core.output.serializers.OutputSerializer;
import lombok.val;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.StringJoiner;

import static com.dtsx.astra.cli.core.output.AstraLogger.Level.QUIET;
import static com.dtsx.astra.cli.utils.StringUtils.*;

@FunctionalInterface
public interface OutputHuman {
    String renderAsHuman(CliContext ctx);

    static OutputHuman response(CharSequence message, @Nullable List<Hint> nextSteps) {
        val s = new StringJoiner(NL + NL).add(trimIndent(message.toString()));

        return (ctx) -> {
            if (ctx.logLevel().ordinal() > QUIET.ordinal() && nextSteps != null) {
                for (val nextStep : nextSteps) {
                    s.add(renderComment(ctx.colors(), nextStep.comment()) + NL + renderCommand(ctx.colors(), nextStep.command()));
                }
            }

            return s.toString();
        };
    }

    static OutputHuman response(CharSequence message) {
        return response(message, null);
    }

    static OutputHuman response(StringJoiner sj) {
        return response(sj.toString(), null);
    }

    static OutputHuman serializeValue(Object o) {
        return (_) -> OutputSerializer.serializeAsHuman(o);
    }
}
