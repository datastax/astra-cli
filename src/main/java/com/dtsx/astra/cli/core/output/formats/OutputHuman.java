package com.dtsx.astra.cli.core.output.formats;

import com.dtsx.astra.cli.core.output.AstraLogger;
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
    String renderAsHuman();

    static OutputHuman response(CharSequence message, @Nullable List<Hint> nextSteps) {
        val s = new StringJoiner(NL + NL).add(trimIndent(message.toString()));

        if (AstraLogger.getLevel().ordinal() > QUIET.ordinal() && nextSteps != null) {
            for (val nextStep : nextSteps) {
                s.add(renderComment(nextStep.comment()) + NL + renderCommand(nextStep.command()));
            }
        }

        return message(s);
    }

    static OutputHuman message(StringJoiner s) {
        return message(s.toString());
    }

    static OutputHuman message(CharSequence s) {
        return s::toString;
    }

    static OutputHuman serializeValue(Object o) {
        return () -> OutputSerializer.trySerializeAsHuman(o);
    }
}
